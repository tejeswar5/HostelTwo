package com.pgm.renter.repository;

import com.pgm.renter.entity.Role;
import com.pgm.renter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByTenantIdAndRole(String tenantId, Role role);

    List<User> findByPhoneNumberInOrEmailIn(List<String> phoneNumbers, List<String> emails);
}
