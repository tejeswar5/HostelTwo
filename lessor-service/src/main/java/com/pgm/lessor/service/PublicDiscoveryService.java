package com.pgm.lessor.service;

import com.pgm.lessor.dto.discovery.*;
import com.pgm.lessor.entity.*;
import com.pgm.lessor.exception.NotFoundException;
import com.pgm.lessor.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Read-only, unauthenticated endpoint renter-service calls over HTTP to browse
 * hostel/room/bed data - this is the one piece of lessor-owned structural data
 * renter-service needs but no longer holds locally after the DB split. Logic here
 * is a straight port of what used to be renter-service's own DiscoveryService,
 * back when both services shared one database.
 */
@Service
@Transactional(readOnly = true)
public class PublicDiscoveryService {

    private final HostelRepository hostelRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final HostelAmenityRepository hostelAmenityRepository;
    private final FloorAmenityRepository floorAmenityRepository;

    public PublicDiscoveryService(
            HostelRepository hostelRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            BedRepository bedRepository,
            HostelAmenityRepository hostelAmenityRepository,
            FloorAmenityRepository floorAmenityRepository) {
        this.hostelRepository = hostelRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.hostelAmenityRepository = hostelAmenityRepository;
        this.floorAmenityRepository = floorAmenityRepository;
    }

    @Cacheable(cacheNames = "publicHostelListing", key = "{#city, #sharingType, #airConditioned}")
    public List<PublicHostelSummaryResponse> discover(String city, Integer sharingType, Boolean airConditioned) {
        if (city == null || city.isEmpty()) return hostelRepository.showAllHostels();
        return hostelRepository.discover(city, sharingType, airConditioned);
    }

    public PublicHostelDetailResponse getHostelDetail(Long hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId).orElseThrow(() -> new NotFoundException("Hostel not found"));
        List<Room> rooms = roomRepository.findByHostelIdOrderByFloorIdAscRoomNumberAsc(hostelId);
        List<Bed> beds = bedRepository.findByHostelIdOrderByFloorIdAscRoomIdAscBedNumberAsc(hostelId);
        List<FloorAmenity> floorAmenities = floorAmenityRepository.findByFloorHostelId(hostelId);

        List<PublicFloorResponse> floors = floorRepository.findByHostelIdOrderByFloorNumberAsc(hostelId).stream()
                .map(floor -> new PublicFloorResponse(
                        floor.getId(),
                        floor.getFloorNumber(),
                        floorAmenities.stream().filter(fa -> fa.getFloor().getId().equals(floor.getId())).map(fa -> fa.getAmenity().getName()).toList(),
                        rooms.stream()
                                .filter(r -> r.getFloor().getId().equals(floor.getId()))
                                .sorted(Comparator.comparing(Room::getRoomNumber))
                                .map(r -> toPublicRoomResponse(r, beds))
                                .toList()))
                .toList();

        List<String> hostelAmenities = hostelAmenityRepository.findByHostelId(hostelId).stream()
                .map(ha -> ha.getAmenity().getName())
                .toList();

        HostelAddress address = hostel.getAddress();
        return new PublicHostelDetailResponse(
                hostel.getId(),
                hostel.getName(),
                hostel.getContactPhone(),
                hostel.getContactEmail(),
                hostel.isHasLift(),
                address == null ? null : address.getArea(),
                address == null ? null : address.getCity(),
                address == null ? null : address.getState(),
                hostelAmenities,
                floors);
    }

    private PublicRoomResponse toPublicRoomResponse(Room room, List<Bed> allBeds) {
        List<PublicBedResponse> beds = allBeds.stream()
                .filter(b -> b.getRoom().getId().equals(room.getId()))
                .map(b -> new PublicBedResponse(b.getId(), b.getBedNumber(), b.getStatus(), b.getCheckOutDate()))
                .toList();
        return new PublicRoomResponse(room.getId(), room.getRoomNumber(), room.getSharingType(), room.isAirConditioned(), room.getMonthlyRent(), beds);
    }
}
