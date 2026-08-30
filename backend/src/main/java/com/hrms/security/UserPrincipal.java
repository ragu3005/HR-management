package com.hrms.security;

import com.hrms.model.User;
import com.hrms.model.Permission;
import com.hrms.model.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String username;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.active = Boolean.TRUE.equals(user.getIsActive());

        Set<GrantedAuthority> auths = new HashSet<>();
        for (Role role : user.getRoles()) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission perm : role.getPermissions()) {
                auths.add(new SimpleGrantedAuthority(perm.getName()));
            }
        }
        this.authorities = auths;
    }

    public boolean hasRole(String roleName) {
        return authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    public boolean hasPermission(String permission) {
        return authorities.stream()
            .anyMatch(a -> a.getAuthority().equals(permission));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return active; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
