package com.expensetracker.controller;

import com.expensetracker.dto.UserRegistrationDto;
import com.expensetracker.service.PasswordEncoderService;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final PasswordEncoderService passwordEncoderService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto dto,
                               BindingResult result,
                               Model model) {
        log.info("Registration attempt for user: {}", dto.getUsername());

        if (userService.existsByUsername(dto.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists!");
        }

        if (userService.existsByEmail(dto.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists!");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            // Encode password before saving
            dto.setPassword(passwordEncoderService.encodePassword(dto.getPassword()));
            userService.registerUser(dto);
            log.info("User registered successfully: {}", dto.getUsername());
            return "redirect:/login?registered";
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
}