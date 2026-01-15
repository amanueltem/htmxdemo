package com.aman.htmxdemo;

import com.aman.htmxdemo.group.GroupMember;
import com.aman.htmxdemo.group.GroupMemberRepository;
import com.aman.htmxdemo.user.*;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

@SpringBootApplication
@EnableJpaAuditing
@ImportRuntimeHints(MyProjectHints.class)
public class HtmxdemoApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("HTMX_DB_URL", Objects.requireNonNull(dotenv.get("HTMX_DB_URL")));
        System.setProperty("HTMX_DB_USERNAME", Objects.requireNonNull(dotenv.get("HTMX_DB_USERNAME")));
        System.setProperty("HTMX_DB_PASSWORD", Objects.requireNonNull(dotenv.get("HTMX_DB_PASSWORD")));
        SpringApplication.run(HtmxdemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository,
                                    UserRepository userRepository,
                                    GroupMemberRepository groupRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            if(groupRepository.count()==0){
                groupRepository.save(GroupMember.builder().name("Gangs of Four").build());
            }
            // Create roles if not exist
            for (RolesEnum roleEnum : RolesEnum.values()) {
                roleRepository.findByName(roleEnum.name())
                        .orElseGet(() -> roleRepository.save(Role.builder().name(roleEnum.name()).build()));
            }

            // Create default admin user
            if (userRepository.findByEmail("amanuel@gmail.com").isEmpty()) {
                Role adminRole = roleRepository.findByName(RolesEnum.ROLE_ADMIN.name())
                        .orElseThrow(() -> new EntityNotFoundException("Admin role not found!"));

                User defaultUser = User.builder()
                        .fullName("Amanuel Temesgen")
                        .email("amanuel@gmail.com")
                        .password(passwordEncoder.encode("asdfasdf"))
                        .enabled(true)
                        .roles(List.of(adminRole))
                        .build();

                userRepository.save(defaultUser);
            }
        };
    }
}

