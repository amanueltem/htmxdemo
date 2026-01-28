package com.aman.htmxdemo.handler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model, HttpServletRequest request) {
        //System.out.println(ex.getMessage());

        // Use Java 25 Pattern Matching for switch to keep it clean
        String message = switch (ex) {
            case OperationNotPermittedException e -> e.getMessage();

            case EntityNotFoundException e -> e.getMessage();

            case DataIntegrityViolationException e -> "You are already registered.";

            default -> {
                // Return generic message to the user (Security)
                yield "Something went wrong for security reasons.";
            }
        };

        model.addAttribute("toastMessage", message);
        model.addAttribute("toastType", "error");

        // HTMX Logic: Return only the toast fragment if it's an HTMX request
        if (request.getHeader("HX-Request") != null) {
            return "fragments/toast :: toast(message=${toastMessage}, type=${toastType})";
        }

        // Fallback for non-HTMX requests (e.g., initial page load errors)
        return "auth/login";
    }
}