package com.aman.htmxdemo.handler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataIntegrityViolationException.class, RuntimeException.class})
    public String handleRuntimeErrors(Exception ex,
                                      Model model,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        String message = "Something went wrong.";
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email")) {
            message = "You already Registered.";
        }

        model.addAttribute("toastMessage", message);
        model.addAttribute("toastType", "error");

        if (request.getHeader("HX-Request") != null) {
            // Explicitly passing parameters to the fragment in the return string
            return "fragments/toast :: toast(message=${toastMessage}, type=${toastType})";
        }

        return "auth/register";
    }
}