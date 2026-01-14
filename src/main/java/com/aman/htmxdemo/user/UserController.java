package com.aman.htmxdemo.user;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest(null,null,null,null)); // Must match th:object
        return "auth/register";
    }


    /* ---------- HTMX Register ---------- */


    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result,
                           HttpServletResponse response,
                           HttpServletRequest httpRequest) {

        if (result.hasErrors()) {
            // If it's an HTMX request, return only the form fragment
            if (httpRequest.getHeader("HX-Request") != null) {
                return "auth/register :: registrationForm";
            }
            // Fallback for standard submission
            return "auth/register";
        }

        userService.register(request);

        // Full page redirect for success
        response.setHeader("HX-Redirect", "/login?success=true");
        return null;
    }


}