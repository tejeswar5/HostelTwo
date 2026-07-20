package com.pgm.lessor.controller;

import com.pgm.lessor.dto.hostel.*;
import com.pgm.lessor.security.UserPrincipal;
import com.pgm.lessor.service.HostelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessor/hostel")
@PreAuthorize("hasRole('LESSOR')")
public class HostelController {

    private final HostelService hostelService;

    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @GetMapping
    public HostelResponse getMyHostel(@AuthenticationPrincipal UserPrincipal principal) {
        return hostelService.getMyHostel(principal);
    }

    @PostMapping
    public ResponseEntity<HostelResponse> createHostel(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody HostelSetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hostelService.createHostel(principal, request));
    }

    @PutMapping
    public HostelResponse updateHostel(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody HostelSetupRequest request) {
        return hostelService.updateHostel(principal, request);
    }

    @GetMapping("/floors")
    public List<FloorResponse> listFloors(@AuthenticationPrincipal UserPrincipal principal) {
        return hostelService.listFloors(principal);
    }

    @PostMapping("/floors")
    public ResponseEntity<FloorResponse> addFloor(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody FloorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hostelService.addFloor(principal, request));
    }

    @DeleteMapping("/floors/{floorId}")
    public ResponseEntity<Void> deleteFloor(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long floorId) {
        hostelService.deleteFloor(principal, floorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/floors/{floorId}/amenities")
    public AmenityResponse assignFloorAmenity(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long floorId, @Valid @RequestBody AmenityAssignRequest request) {
        return hostelService.assignFloorAmenity(principal, floorId, request);
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> addRoom(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hostelService.addRoom(principal, request));
    }

    @PutMapping("/rooms/{roomId}")
    public RoomResponse updateRoom(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long roomId, @Valid @RequestBody RoomRequest request) {
        return hostelService.updateRoom(principal, roomId, request);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long roomId) {
        hostelService.deleteRoom(principal, roomId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/amenities")
    public AmenityResponse assignRoomAmenity(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long roomId, @Valid @RequestBody AmenityAssignRequest request) {
        return hostelService.assignRoomAmenity(principal, roomId, request);
    }

    @GetMapping("/beds")
    public List<BedResponse> getBedBoard(@AuthenticationPrincipal UserPrincipal principal) {
        return hostelService.getBedBoard(principal);
    }

    @PostMapping("/beds")
    public ResponseEntity<BedResponse> addBed(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody BedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hostelService.addBed(principal, request));
    }

    @DeleteMapping("/beds/{bedId}")
    public ResponseEntity<Void> deleteBed(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long bedId) {
        hostelService.deleteBed(principal, bedId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/beds/{bedId}/maintenance")
    public BedResponse markMaintenance(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long bedId, @Valid @RequestBody MaintenanceRequest request) {
        return hostelService.markBedMaintenance(principal, bedId, request);
    }

    @PatchMapping("/beds/{bedId}/available")
    public BedResponse markAvailable(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long bedId) {
        return hostelService.markBedAvailable(principal, bedId);
    }

    @PatchMapping("/beds/{bedId}/next-month-rent")
    public BedResponse setNextMonthRentPaid(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long bedId, @RequestParam boolean paid) {
        return hostelService.setNextMonthRentPaid(principal, bedId, paid);
    }

    @PostMapping("/amenities")
    public AmenityResponse assignHostelAmenity(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AmenityAssignRequest request) {
        return hostelService.assignHostelAmenity(principal, request);
    }
}
