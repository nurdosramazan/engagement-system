package com.epam.engagement_system.security;

import com.epam.engagement_system.domain.ApplicationUser;
import com.epam.engagement_system.domain.Role;
import com.epam.engagement_system.domain.enums.RoleType;
import com.epam.engagement_system.repository.RoleRepository;
import com.epam.engagement_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String phoneNumber) {
        ApplicationUser user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    Role userRole = roleRepository.findByName(RoleType.USER)
                            .orElseThrow(() -> new RuntimeException("Error: Default USER role not found."));

                    ApplicationUser newUser = new ApplicationUser(phoneNumber);
                    newUser.setRoles(Set.of(userRole));
                    return userRepository.save(newUser);
                });
        return UserPrincipal.create(user);
    }
}
