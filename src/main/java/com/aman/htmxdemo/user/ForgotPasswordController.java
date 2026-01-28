package com.aman.htmxdemo.user;

import com.aman.htmxdemo.handler.OperationNotPermittedException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
@Controller
@RequestMapping("/forgot-password") // Root path for this controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final PasswordResetService service;
    private final TokenRepository tokenRepo;
    private final EmailService emailService;

    // Added @PostMapping and fixed path to /forgot-password/process
    @PostMapping("/process")
    public String processRequest(@RequestParam String email, Model model) throws MessagingException {
        String token = service.createResetToken(email);
        emailService.sendPasswordResetEmail(email, token);

        model.addAttribute("toastMessage", "Reset link sent successfully!");
        model.addAttribute("toastType", "success");
        return "fragments/toast :: toast(message=${toastMessage}, type=${toastType})";
    }

    // This handles the link clicked in the email
    @GetMapping("/reset")
    public String validateToken(@RequestParam String token, Model model) {
        Token resetToken = tokenRepo.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new OperationNotPermittedException("The reset link is expired or invalid."));

        model.addAttribute("token", token);
        return "auth/reset-password-page";
    }

    @GetMapping("/show")
    public String getForgotPasswordForm() {
        // Return from the auth folder to keep security templates together
        return "auth/forgot-password-fragment :: forgot-password-form";
    }
    @PostMapping("/update")
    public String updatePassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            jakarta.servlet.http.HttpServletResponse response) {

        // The Service handles the final check.
        // If they don't match, GlobalExceptionHandler pops a Red Toast.
        service.updateUserPassword(token, newPassword, confirmPassword);
        response.setHeader("HX-Redirect", "/login?resetSuccess");

        return "fragments/toast :: toast(message='Security updated!', type='success')";
    }
}