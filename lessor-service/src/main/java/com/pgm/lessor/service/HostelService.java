package com.pgm.lessor.service;

import com.pgm.lessor.audit.Auditable;
import com.pgm.lessor.dto.hostel.*;
import com.pgm.lessor.entity.*;
import com.pgm.lessor.exception.ConflictException;
import com.pgm.lessor.exception.ForbiddenException;
import com.pgm.lessor.exception.NotFoundException;
import com.pgm.lessor.repository.*;
import com.pgm.lessor.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Owns the lessor's single hostel: its address, floors, rooms, beds and amenities.
 * Every mutation resolves the hostel by the caller's tenant id (never a client
 * header) and checks that the floor/room/bed being touched actually belongs to it.
 */
@Service
@Transactional(readOnly = true)
public class HostelService {

    private final HostelRepository hostelRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final AmenityRepository amenityRepository;
    private final HostelAmenityRepository hostelAmenityRepository;
    private final FloorAmenityRepository floorAmenityRepository;
    private final RoomAmenityRepository roomAmenityRepository;

    public HostelService(
            HostelRepository hostelRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            BedRepository bedRepository,
            AmenityRepository amenityRepository,
            HostelAmenityRepository hostelAmenityRepository,
            FloorAmenityRepository floorAmenityRepository,
            RoomAmenityRepository roomAmenityRepository) {
        this.hostelRepository = hostelRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.amenityRepository = amenityRepository;
        this.hostelAmenityRepository = hostelAmenityRepository;
        this.floorAmenityRepository = floorAmenityRepository;
        this.roomAmenityRepository = roomAmenityRepository;
    }

    public Hostel requireMyHostel(UserPrincipal principal) {
        return hostelRepository.findByTenantId(principal.tenantId())
                .orElseThrow(() -> new NotFoundException("You haven't set up your hostel yet"));
    }

    public HostelResponse getMyHostel(UserPrincipal principal) {
        return toHostelResponse(requireMyHostel(principal));
    }

    @Transactional
    @Auditable(action = "HOSTEL_CREATED", entityType = "HOSTEL")
    public HostelResponse createHostel(UserPrincipal principal, HostelSetupRequest request) {
        if (hostelRepository.existsByTenantId(principal.tenantId())) {
            throw new ConflictException("You already have a hostel set up");
        }
        HostelAddress address = HostelAddress.builder()
                .buildingNameOrNumber(request.buildingNameOrNumber())
                .street(request.street())
                .area(request.area())
                .city(request.city())
                .state(request.state())
                .pinCode(request.pinCode())
                .build();
        Hostel hostel = Hostel.builder()
                .tenantId(principal.tenantId())
                .name(request.name())
                .address(address)
                .contactPhone(request.contactPhone())
                .contactEmail(request.contactEmail())
                .hasLift(request.hasLift())
                .build();
        return toHostelResponse(hostelRepository.save(hostel));
    }

    @Transactional
    @Auditable(action = "HOSTEL_UPDATED", entityType = "HOSTEL")
    public HostelResponse updateHostel(UserPrincipal principal, HostelSetupRequest request) {
        Hostel hostel = requireMyHostel(principal);
        hostel.setName(request.name());
        hostel.setContactPhone(request.contactPhone());
        hostel.setContactEmail(request.contactEmail());
        hostel.setHasLift(request.hasLift());
        HostelAddress address = hostel.getAddress();
        if (address == null) {
            address = new HostelAddress();
            hostel.setAddress(address);
        }
        address.setBuildingNameOrNumber(request.buildingNameOrNumber());
        address.setStreet(request.street());
        address.setArea(request.area());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPinCode(request.pinCode());
        return toHostelResponse(hostel);
    }

    public List<FloorResponse> listFloors(UserPrincipal principal) {
        Hostel hostel = requireMyHostel(principal);
        List<Room> rooms = roomRepository.findByHostelIdOrderByFloorIdAscRoomNumberAsc(hostel.getId());
        List<Bed> beds = bedRepository.findByHostelIdOrderByFloorIdAscRoomIdAscBedNumberAsc(hostel.getId());
        return floorRepository.findByHostelIdOrderByFloorNumberAsc(hostel.getId()).stream()
                .map(floor -> toFloorResponse(floor, rooms, beds))
                .toList();
    }

    @Transactional
    @Auditable(action = "FLOOR_ADDED", entityType = "FLOOR")
    public FloorResponse addFloor(UserPrincipal principal, FloorRequest request) {
        Hostel hostel = requireMyHostel(principal);
        if (floorRepository.existsByHostelIdAndFloorNumber(hostel.getId(), request.floorNumber())) {
            throw new ConflictException("Floor " + request.floorNumber() + " already exists");
        }
        Floor floor = floorRepository.save(Floor.builder().hostel(hostel).floorNumber(request.floorNumber()).build());
        return toFloorResponse(floor, List.of(), List.of());
    }

    @Transactional
    @Auditable(action = "FLOOR_DELETED", entityType = "FLOOR")
    public void deleteFloor(UserPrincipal principal, Long floorId) {
        Floor floor = findOwnedFloor(principal, floorId);
        floorRepository.delete(floor);
    }

    @Transactional
    @Auditable(action = "ROOM_ADDED", entityType = "ROOM")
    public RoomResponse addRoom(UserPrincipal principal, RoomRequest request) {
        Hostel hostel = requireMyHostel(principal);
        Floor floor = findOwnedFloor(principal, request.floorId());
        if (roomRepository.existsByFloorIdAndRoomNumber(floor.getId(), request.roomNumber())) {
            throw new ConflictException("Room " + request.roomNumber() + " already exists on this floor");
        }
        Room room = Room.builder()
                .hostel(hostel)
                .floor(floor)
                .roomNumber(request.roomNumber())
                .capacity(request.capacity())
                .monthlyRent(request.monthlyRent())
                .sharingType(request.sharingType())
                .airConditioned(request.airConditioned())
                .build();
        room = roomRepository.save(room);
        return toRoomResponse(room, List.of());
    }

    @Transactional
    @Auditable(action = "ROOM_UPDATED", entityType = "ROOM")
    public RoomResponse updateRoom(UserPrincipal principal, Long roomId, RoomRequest request) {
        Room room = findOwnedRoom(principal, roomId);
        room.setRoomNumber(request.roomNumber());
        room.setCapacity(request.capacity());
        room.setMonthlyRent(request.monthlyRent());
        room.setSharingType(request.sharingType());
        room.setAirConditioned(request.airConditioned());
        List<Bed> beds = bedRepository.findByHostelIdOrderByFloorIdAscRoomIdAscBedNumberAsc(room.getHostel().getId())
                .stream().filter(b -> b.getRoom().getId().equals(room.getId())).toList();
        return toRoomResponse(room, beds);
    }

    @Transactional
    @Auditable(action = "ROOM_DELETED", entityType = "ROOM")
    public void deleteRoom(UserPrincipal principal, Long roomId) {
        Room room = findOwnedRoom(principal, roomId);
        roomRepository.delete(room);
    }

    public List<BedResponse> getBedBoard(UserPrincipal principal) {
        Hostel hostel = requireMyHostel(principal);
        return bedRepository.findByHostelIdOrderByFloorIdAscRoomIdAscBedNumberAsc(hostel.getId()).stream()
                .map(this::toBedResponse)
                .toList();
    }

    @Transactional
    @Auditable(action = "BED_ADDED", entityType = "BED")
    public BedResponse addBed(UserPrincipal principal, BedRequest request) {
        Hostel hostel = requireMyHostel(principal);
        Room room = findOwnedRoom(principal, request.roomId());
        if (bedRepository.existsByRoomIdAndBedNumber(room.getId(), request.bedNumber())) {
            throw new ConflictException("Bed " + request.bedNumber() + " already exists in this room");
        }
        Bed bed = Bed.builder()
                .hostel(hostel)
                .floor(room.getFloor())
                .room(room)
                .bedNumber(request.bedNumber())
                .status(BedStatus.AVAILABLE)
                .nextMonthRentPaid(false)
                .build();
        return toBedResponse(bedRepository.save(bed));
    }

    @Transactional
    @Auditable(action = "BED_DELETED", entityType = "BED")
    public void deleteBed(UserPrincipal principal, Long bedId) {
        Bed bed = findOwnedBed(principal, bedId);
        if (bed.getStatus() == BedStatus.BOOKED) {
            throw new ConflictException("Cannot delete a bed that is currently booked");
        }
        bedRepository.delete(bed);
    }

    @Transactional
    @Auditable(action = "BED_MAINTENANCE", entityType = "BED")
    public BedResponse markBedMaintenance(UserPrincipal principal, Long bedId, MaintenanceRequest request) {
        Bed bed = lockOwnedBed(principal, bedId);
        if (bed.getStatus() == BedStatus.BOOKED) {
            throw new ConflictException("Bed is currently booked; it cannot be marked for maintenance");
        }
        bed.markUnderMaintenance(request.reason());
        return toBedResponse(bed);
    }

    @Transactional
    @Auditable(action = "BED_AVAILABLE", entityType = "BED")
    public BedResponse markBedAvailable(UserPrincipal principal, Long bedId) {
        Bed bed = lockOwnedBed(principal, bedId);
        bed.markAvailable();
        return toBedResponse(bed);
    }

    @Transactional
    @Auditable(action = "BED_RENT_STATUS_UPDATED", entityType = "BED")
    public BedResponse setNextMonthRentPaid(UserPrincipal principal, Long bedId, boolean paid) {
        Bed bed = findOwnedBed(principal, bedId);
        bed.setNextMonthRentPaid(paid);
        return toBedResponse(bed);
    }

    @Transactional
    @Auditable(action = "AMENITY_ASSIGNED", entityType = "HOSTEL")
    public AmenityResponse assignHostelAmenity(UserPrincipal principal, AmenityAssignRequest request) {
        Hostel hostel = requireMyHostel(principal);
        Amenity amenity = findOrCreateAmenity(request.name());
        HostelAmenity link = hostelAmenityRepository.findByHostelId(hostel.getId()).stream()
                .filter(ha -> ha.getAmenity().getId().equals(amenity.getId()))
                .findFirst()
                .orElseGet(() -> HostelAmenity.builder().hostel(hostel).amenity(amenity).quantity(0).build());
        link.setQuantity(request.quantity());
        link = hostelAmenityRepository.save(link);
        return new AmenityResponse(amenity.getId(), amenity.getName(), link.getQuantity());
    }

    @Transactional
    @Auditable(action = "AMENITY_ASSIGNED", entityType = "FLOOR")
    public AmenityResponse assignFloorAmenity(UserPrincipal principal, Long floorId, AmenityAssignRequest request) {
        Floor floor = findOwnedFloor(principal, floorId);
        Amenity amenity = findOrCreateAmenity(request.name());
        FloorAmenity link = floorAmenityRepository.findByFloorId(floor.getId()).stream()
                .filter(fa -> fa.getAmenity().getId().equals(amenity.getId()))
                .findFirst()
                .orElseGet(() -> FloorAmenity.builder().floor(floor).amenity(amenity).quantity(0).build());
        link.setQuantity(request.quantity());
        link = floorAmenityRepository.save(link);
        return new AmenityResponse(amenity.getId(), amenity.getName(), link.getQuantity());
    }

    @Transactional
    @Auditable(action = "AMENITY_ASSIGNED", entityType = "ROOM")
    public AmenityResponse assignRoomAmenity(UserPrincipal principal, Long roomId, AmenityAssignRequest request) {
        Room room = findOwnedRoom(principal, roomId);
        Amenity amenity = findOrCreateAmenity(request.name());
        RoomAmenity link = roomAmenityRepository.findByRoomId(room.getId()).stream()
                .filter(ra -> ra.getAmenity().getId().equals(amenity.getId()))
                .findFirst()
                .orElseGet(() -> RoomAmenity.builder().room(room).amenity(amenity).quantity(0).build());
        link.setQuantity(request.quantity());
        link = roomAmenityRepository.save(link);
        return new AmenityResponse(amenity.getId(), amenity.getName(), link.getQuantity());
    }

    private Amenity findOrCreateAmenity(String name) {
        return amenityRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> amenityRepository.save(Amenity.builder().name(name).build()));
    }

    private Floor findOwnedFloor(UserPrincipal principal, Long floorId) {
        Hostel hostel = requireMyHostel(principal);
        Floor floor = floorRepository.findById(floorId).orElseThrow(() -> new NotFoundException("Floor not found"));
        if (!floor.getHostel().getId().equals(hostel.getId())) {
            throw new ForbiddenException("Floor does not belong to your hostel");
        }
        return floor;
    }

    private Room findOwnedRoom(UserPrincipal principal, Long roomId) {
        Hostel hostel = requireMyHostel(principal);
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
        if (!room.getHostel().getId().equals(hostel.getId())) {
            throw new ForbiddenException("Room does not belong to your hostel");
        }
        return room;
    }

    private Bed findOwnedBed(UserPrincipal principal, Long bedId) {
        Hostel hostel = requireMyHostel(principal);
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new NotFoundException("Bed not found"));
        if (!bed.getHostel().getId().equals(hostel.getId())) {
            throw new ForbiddenException("Bed does not belong to your hostel");
        }
        return bed;
    }

    private Bed lockOwnedBed(UserPrincipal principal, Long bedId) {
        Hostel hostel = requireMyHostel(principal);
        Bed bed = bedRepository.findByIdForUpdate(bedId).orElseThrow(() -> new NotFoundException("Bed not found"));
        if (!bed.getHostel().getId().equals(hostel.getId())) {
            throw new ForbiddenException("Bed does not belong to your hostel");
        }
        return bed;
    }

    private HostelResponse toHostelResponse(Hostel hostel) {
        HostelAddress address = hostel.getAddress();
        return new HostelResponse(
                hostel.getId(),
                hostel.getName(),
                hostel.getContactPhone(),
                hostel.getContactEmail(),
                hostel.isHasLift(),
                address == null ? null : address.getBuildingNameOrNumber(),
                address == null ? null : address.getStreet(),
                address == null ? null : address.getArea(),
                address == null ? null : address.getCity(),
                address == null ? null : address.getState(),
                address == null ? null : address.getPinCode());
    }

    private FloorResponse toFloorResponse(Floor floor, List<Room> allRooms, List<Bed> allBeds) {
        List<RoomResponse> rooms = allRooms.stream()
                .filter(r -> r.getFloor().getId().equals(floor.getId()))
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .map(r -> toRoomResponse(r, allBeds.stream().filter(b -> b.getRoom().getId().equals(r.getId())).toList()))
                .toList();
        return new FloorResponse(floor.getId(), floor.getFloorNumber(), rooms);
    }

    private RoomResponse toRoomResponse(Room room, List<Bed> beds) {
        return new RoomResponse(
                room.getId(),
                room.getFloor().getId(),
                room.getRoomNumber(),
                room.getCapacity(),
                room.getMonthlyRent(),
                room.getSharingType(),
                room.isAirConditioned(),
                beds.stream().map(this::toBedResponse).toList());
    }

    private BedResponse toBedResponse(Bed bed) {
        return new BedResponse(
                bed.getId(),
                bed.getFloor().getId(),
                bed.getFloor().getFloorNumber(),
                bed.getRoom().getId(),
                bed.getRoom().getRoomNumber(),
                bed.getBedNumber(),
                bed.getStatus(),
                bed.getCheckInDate(),
                bed.getCheckOutDate(),
                bed.isNextMonthRentPaid(),
                bed.getMaintenanceReason(),
                bed.getRoom().getMonthlyRent());
    }
}
