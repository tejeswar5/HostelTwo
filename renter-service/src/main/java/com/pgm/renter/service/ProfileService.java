package com.pgm.renter.service;

import com.pgm.renter.audit.Auditable;
import com.pgm.renter.dto.profile.ProfileResponse;
import com.pgm.renter.dto.profile.ProfileUpdateRequest;
import com.pgm.renter.entity.User;
import com.pgm.renter.exception.ConflictException;
import com.pgm.renter.exception.NotFoundException;
import com.pgm.renter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponse get(Long userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    @Auditable(action = "PROFILE_UPDATED", entityType = "USER")
    public ProfileResponse update(Long userId, ProfileUpdateRequest request) {
        User user = findUser(userId);
        userRepository.findByEmail(request.email())
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> { throw new ConflictException("That email is already in use"); });
        user.setFname(request.fname());
        user.setLname(request.lname());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setProfilePictureUrl(request.profilePictureUrl());
        return toResponse(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getId(), user.getFname(), user.getLname(), user.getEmail(), user.getPhoneNumber(), user.getProfilePictureUrl(), user.getRole());
    }
}
