package com.app.spring.security.service;

import com.app.spring.security.persistence.entity.UserEntity;
import com.app.spring.security.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("The username" + username + " was not found"));

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();


        userEntity.getRoles().forEach(roleEntity -> {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_".concat(roleEntity.getRoleEnum().name())));
        });

        userEntity.getRoles().stream()
                .flatMap(roleEntity -> roleEntity.getPermissions().stream())
                .forEach(permissionEntity -> {
                    grantedAuthorities.add(new SimpleGrantedAuthority(permissionEntity.getName()));
                });


        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNonExpired(),
                userEntity.isCredentialsNonExpired(),
                userEntity.isAccountNonLocked(),
                grantedAuthorities);
    }
}
