package com.epam.engagement_system.repository;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {
    Optional<ApplicationUser> findByPhoneNumber(String phoneNumber);
    List<ApplicationUser> findByRoles_Name(RoleType role);
}
