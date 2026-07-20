package com.pgm.lessor.controller;

import com.pgm.lessor.dto.profile.ProfileResponse;
import com.pgm.lessor.dto.profile.ProfileUpdateRequest;
import com.pgm.lessor.security.UserPrincipal;
import com.pgm.lessor.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessor/profile")
@PreAuthorize("hasRole('LESSOR')")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponse get(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.get(principal.userId());
    }

    @PutMapping
    public ProfileResponse update(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.update(principal.userId(), request);
    }
}
