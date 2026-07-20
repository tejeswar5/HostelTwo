package com.pgm.renter.service;

import com.pgm.renter.dto.connections.ConnectionResponse;
import com.pgm.renter.dto.connections.ConnectionsRequest;
import com.pgm.renter.entity.BookingStatus;
import com.pgm.renter.entity.User;
import com.pgm.renter.repository.BookingRepository;
import com.pgm.renter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * "A few connections are here" - given phone numbers / emails the renter already
 * knows (e.g. from their phone contacts, entered client-side), find which of those
 * already-registered users currently have an approved booking at the same hostel.
 * No contacts-permission/OAuth integration - the client supplies the list explicitly.
 */
@Service
@Transactional(readOnly = true)
public class ConnectionsService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public ConnectionsService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<ConnectionResponse> findAtHostel(Long requestingRenterId, Long hostelId, ConnectionsRequest request) {
        List<String> phones = request.phoneNumbers() == null ? List.of() : request.phoneNumbers();
        List<String> emails = request.emails() == null ? List.of() : request.emails();
        if (phones.isEmpty() && emails.isEmpty()) {
            return List.of();
        }
        List<User> matches = userRepository.findByPhoneNumberInOrEmailIn(phones, emails);
        List<Long> candidateIds = matches.stream().map(User::getId).filter(id -> !id.equals(requestingRenterId)).toList();
        if (candidateIds.isEmpty()) {
            return List.of();
        }
        return bookingRepository.findByHostelIdAndStatusAndRenterIdIn(hostelId, BookingStatus.APPROVED, candidateIds).stream()
                .map(booking -> new ConnectionResponse(
                        booking.getRenter().getId(),
                        booking.getRenter().getFname() + " " + booking.getRenter().getLname(),
                        booking.getRenter().getPhoneNumber(),
                        booking.getRoomNumber()))
                .toList();
    }
}
