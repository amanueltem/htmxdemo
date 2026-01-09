package com.aman.htmxdemo.user;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue
    private UUID id;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    @OneToOne
    @JoinColumn(name = "userId",nullable = false)
    private User user;
}