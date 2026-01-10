package com.aman.htmxdemo.user;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;


    /* ---------- Pages ---------- */


    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }


    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }


    /* ---------- HTMX Register ---------- */


    @PostMapping("/register")
    public String register(
            @Valid RegisterRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("message", "Invalid input");
            return "fragments/alert-error";
        }

        try {
            userService.register(request);
            model.addAttribute("message", "Registration successful. You can now log in.");
            return "fragments/alert-success";
        } catch (DataIntegrityViolationException ex) {
            // Most likely duplicate email
            model.addAttribute("message", "Email already exists. Please use a different email.");
            return "fragments/alert-error";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("message", ex.getMessage());
            return "fragments/alert-error";
        }
    }

}