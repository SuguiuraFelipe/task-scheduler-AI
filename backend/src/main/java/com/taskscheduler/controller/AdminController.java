package com.taskscheduler.controller;

import com.taskscheduler.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ReminderService reminderService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @PostMapping("/trigger-reminders")
    public void triggerReminders() {
        reminderService.checkAndSendReminders();
    }

    @PostMapping("/test-email")
    public Map<String, String> testEmail(@RequestParam(required = false) String to) {
        Map<String, String> result = new HashMap<>();
        try {
            if (fromEmail == null || fromEmail.isBlank()) {
                result.put("status", "error");
                result.put("message", "spring.mail.username / GMAIL_EMAIL não configurado");
                return result;
            }

            String targetEmail = (to != null && !to.isBlank()) ? to : fromEmail;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(targetEmail);
            message.setSubject("Test Email from Task Scheduler");
            message.setText("This is a test email to verify Gmail configuration is working.");

            mailSender.send(message);
            result.put("status", "success");
            result.put("message", "Test email sent successfully!");
            result.put("to", targetEmail);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("cause", e.getCause() != null ? e.getCause().getMessage() : "No cause");
        }
        return result;
    }
}
