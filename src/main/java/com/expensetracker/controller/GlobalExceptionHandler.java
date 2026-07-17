package com.expensetracker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("Exception occurred: {}", e.getMessage(), e);
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        model.addAttribute("error", e.getMessage());
        return "error";
    }
}