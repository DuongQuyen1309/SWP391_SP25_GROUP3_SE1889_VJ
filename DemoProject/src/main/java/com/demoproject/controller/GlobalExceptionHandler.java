package com.demoproject.controller;

import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500"; // đảm bảo file này tồn tại!
    }

    @ExceptionHandler(SpelEvaluationException.class)
    public String handleSpelException(SpelEvaluationException ex, Model model) {
        model.addAttribute("errorMessage", "Lỗi hiển thị dữ liệu (Thymeleaf): " + ex.getMessage());
        return "error/500";
    }
}

