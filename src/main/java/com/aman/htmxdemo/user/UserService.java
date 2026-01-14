package com.aman.htmxdemo.user;

import com.aman.htmxdemo.group.GroupMember;
import com.aman.htmxdemo.group.GroupMemberRepository;
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
    private final GroupMemberRepository groupRepo;
    @Transactional
    public void register(RegisterRequest request){
        Role role=roleRepo.findByName(RolesEnum.ROLE_INPUTTER.name()).orElseThrow(
                ()-> new EntityNotFoundException("Role user not found.")
        );
        GroupMember defaultMember=groupRepo.findByName("Gangs of Four").orElseThrow(
                ()-> new EntityNotFoundException("Gangs of Four group not found.")
        );
        repo.save(User.builder()
                        .fullName(request.fullName())
                        .email(request.email())
                        .phoneNumber(request.phoneNumber())
                        .password(passwordEncoder.encode(request.password()))
                        .accountLocked(false)
                        .enabled(false)
                        .roles(List.of(role))
                        .groupMember(defaultMember)
                .build());
    }
}
