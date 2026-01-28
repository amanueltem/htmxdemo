package com.aman.htmxdemo.user;

import com.aman.htmxdemo.handler.OperationNotPermittedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if(user.isAccountLocked() || !user.isEnabled()){
            throw  new OperationNotPermittedException("User is disabled.");
        }

        // Clean up any existing tokens for this user
        tokenRepository.deleteByUser(user);
        tokenRepository.flush();

        String tokenValue = UUID.randomUUID().toString();
        Token token = Token.builder()
                .token(tokenValue)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        tokenRepository.save(token);
        return tokenValue;
    }
    @Transactional
    public void updateUserPassword(String token, String newPass, String confirmPass) {
        // Safety check: if someone bypasses the HTML, this catches them.
        if (!newPass.equals(confirmPass)) {
            throw new OperationNotPermittedException("Passwords must match.");
        }

        Token resetToken = tokenRepository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new OperationNotPermittedException("Link expired."));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);

        // Wipe the token so it can't be used again
      tokenRepository.delete(resetToken);
    }
}