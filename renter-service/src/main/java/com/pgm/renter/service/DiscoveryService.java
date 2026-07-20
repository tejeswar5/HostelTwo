package com.pgm.renter.service;

import com.pgm.renter.dto.discovery.DiscoverHostelResponse;
import com.pgm.renter.dto.discovery.HostelDetailResponse;
import com.pgm.renter.exception.NotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Hostel/room/bed structure moved entirely into lessor-service's database after
 * the split - this service no longer holds it locally, so browsing now means a
 * synchronous, unauthenticated HTTP call to lessor-service's public discovery
 * endpoint rather than a local repository query. This is a deliberate choice over
 * replicating hostel structure via Kafka: browse reads are simple, infrequent
 * relative to booking traffic, and don't need to survive lessor-service being down,
 * so a live call is simpler than maintaining a second copy of someone else's data.
 * The short-TTL cache (see app.cache in application.properties) is what keeps this
 * off the hot path for repeated identical searches.
 */
@Service
public class DiscoveryService {

    private final RestClient lessorServiceClient;

    public DiscoveryService(RestClient lessorServiceClient) {
        this.lessorServiceClient = lessorServiceClient;
    }

    @Cacheable(cacheNames = "nearbyHostels", key = "{#city, #sharingType, #airConditioned}")
    public List<DiscoverHostelResponse> discover(String city, Integer sharingType, Boolean airConditioned) {
        return lessorServiceClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/public/hostels")
                        .queryParamIfPresent("city", java.util.Optional.ofNullable(city))
                        .queryParamIfPresent("sharingType", java.util.Optional.ofNullable(sharingType))
                        .queryParamIfPresent("airConditioned", java.util.Optional.ofNullable(airConditioned))
                        .build())
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<DiscoverHostelResponse>>() {
                });
    }

    public HostelDetailResponse getHostelDetail(Long hostelId) {
        try {
            return lessorServiceClient.get()
                    .uri("/api/public/hostels/{hostelId}", hostelId)
                    .retrieve()
                    .body(HostelDetailResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Hostel not found");
        }
    }
}
