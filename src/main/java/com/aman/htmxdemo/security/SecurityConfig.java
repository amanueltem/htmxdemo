package com.aman.htmxdemo.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorize requests
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/login", "/css/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Form login configuration
                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // Remember-me support (optional)
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey12345")            // secret key for tokens
                        .tokenValiditySeconds(7 * 24 * 60 * 60)    // 7 days
                        .tokenRepository(new InMemoryTokenRepositoryImpl()) // simple in-memory
                )

                // CSRF configuration
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // ignore H2 console if used
                )

                // Headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // allow H2 console
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
