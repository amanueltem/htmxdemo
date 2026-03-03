package com.aman.htmxdemo;

import com.aman.htmxdemo.deposit.Deposit;
import com.aman.htmxdemo.deposit.DepositRepository;
import com.aman.htmxdemo.expense.Expense;
import com.aman.htmxdemo.expense.ExpenseRepository;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
@SpringBootApplication
@EnableJpaAuditing
@ImportRuntimeHints(MyProjectHints.class)
@EnableAsync
public class HtmxdemoApplication {

     static void main(String[] args) {
        // 1. Configure Dotenv to be optional (Safe for Docker/Native builds)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // 2. Map variables only if they exist (Prefer System Env for Production)
        setSystemPropertyIfNotNull(dotenv, "HTMX_DB_URL");
        setSystemPropertyIfNotNull(dotenv, "HTMX_DB_USERNAME");
        setSystemPropertyIfNotNull(dotenv, "HTMX_DB_PASSWORD");
         setSystemPropertyIfNotNull(dotenv, "GMAIL_APP_PASSWORD");

        SpringApplication.run(HtmxdemoApplication.class, args);
    }

    private static void setSystemPropertyIfNotNull(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null) {
            System.setProperty(key, value);
        }
    }

    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository,
                                    UserRepository userRepository,
                                    GroupMemberRepository groupRepository,
                                    ExpenseRepository expenseRepository,
                                    DepositRepository depositRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Ensure Group Exists and capture it
            GroupMember mainGroup = groupRepository.findByName("Gangs of Four")
                    .orElseGet(() -> groupRepository.save(GroupMember.builder().name("Gangs of Four").build()));

            // 2. Create roles
            for (RolesEnum roleEnum : RolesEnum.values()) {
                roleRepository.findByName(roleEnum.name())
                        .orElseGet(() -> roleRepository.save(Role.builder().name(roleEnum.name()).build()));
            }

            // 3. Create default admin and ASSIGN to group
            if (userRepository.findByEmail("amanuel@gmail.com").isEmpty()) {
                Role adminRole = roleRepository.findByName(RolesEnum.ROLE_ADMIN.name())
                        .orElseThrow(() -> new EntityNotFoundException("Admin role not found!"));

                User defaultUser = User.builder()
                        .fullName("Amanuel Temesgen")
                        .email("amanuel@gmail.com")
                        .password(passwordEncoder.encode("asdfasdf"))
                        .enabled(true)
                        .roles(List.of(adminRole))
                        .groupMember(mainGroup) // Assign admin to the group too!
                        .build();

                userRepository.save(defaultUser);
            }

            /* 4. Migrate existing orphan data to this group
            // We only do this for records where group is currently null to avoid redundant writes
            List<Expense> expensesToUpdate = expenseRepository.findAll().stream()
                    .filter(e -> e.getGroup() == null)
                    .peek(e -> e.setGroup(mainGroup))
                    .toList();
            if (!expensesToUpdate.isEmpty()) expenseRepository.saveAll(expensesToUpdate);

            List<Deposit> depositsToUpdate = depositRepository.findAll().stream()
                    .filter(d -> d.getGroup() == null)
                    .peek(d -> d.setGroup(mainGroup))
                    .toList();
            if (!depositsToUpdate.isEmpty()) depositRepository.saveAll(depositsToUpdate);

            System.out.println("Migration and Initialization complete.");*/
        };
    }
}

