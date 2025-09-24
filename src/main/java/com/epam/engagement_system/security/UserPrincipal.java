package com.epam.engagement_system.security;

import com.epam.engagement_system.domain.ApplicationUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    @Getter
    private final Long id;
    private final String phoneNumber;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String phoneNumber,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.authorities = authorities;
    }

    public static UserPrincipal create(ApplicationUser user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());

        return new UserPrincipal(user.getId(), user.getPhoneNumber(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }
}
