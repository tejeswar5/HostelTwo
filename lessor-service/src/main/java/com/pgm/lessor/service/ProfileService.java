package com.pgm.lessor.service;

import com.pgm.lessor.audit.Auditable;
import com.pgm.lessor.dto.profile.ProfileResponse;
import com.pgm.lessor.dto.profile.ProfileUpdateRequest;
import com.pgm.lessor.entity.User;
import com.pgm.lessor.exception.ConflictException;
import com.pgm.lessor.exception.NotFoundException;
import com.pgm.lessor.repository.UserRepository;
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
