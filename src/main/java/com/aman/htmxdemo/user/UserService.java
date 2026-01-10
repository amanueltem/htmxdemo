package com.aman.htmxdemo.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public void register(RegisterRequest request){
        Role role=roleRepo.findByName(RolesEnum.ROLE_USER.name()).orElseThrow(
                ()-> new EntityNotFoundException("Role user not found.")
        );
        repo.save(User.builder()
                        .fullName(request.fullName())
                        .email(request.email())
                        .phoneNumber(request.phoneNumber())
                        .password(passwordEncoder.encode(request.password()))
                        .accountLocked(false)
                        .enabled(false)
                        .roles(List.of(role))
                .build());
    }
}
