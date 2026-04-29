package com.taskscheduler.service;

import com.taskscheduler.entity.ReminderLog;
import com.taskscheduler.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private boolean isConfigured() {
        return fromEmail != null && !fromEmail.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public void sendTaskCreatedEmail(String toEmail, String userName, String taskTitle,
                                      String taskDescription, LocalDateTime dueDate,
                                      Task.TaskPriority priority) {
        String subject = "Tarefa Criada: " + taskTitle;
        String html = buildCreatedHtml(userName, taskTitle, taskDescription, dueDate, priority);

        if (!isConfigured()) {
            log.warn("Serviço de email não configurado completamente; a usar modo mock.");
            logMockEmail(toEmail, subject, taskTitle, taskDescription, dueDate, priority, "CRIAÇÃO");
            return;
        }
        sendHtml(toEmail, subject, html, "confirmação de criação");
    }

    public void sendReminderEmail(String toEmail, String userName, String taskTitle,
                                   String taskDescription, LocalDateTime dueDate,
                                   Task.TaskPriority priority,
                                   ReminderLog.ReminderType reminderType) {
        String subject = reminderType == ReminderLog.ReminderType.DAY_BEFORE
                ? "Lembrete: Tarefa vencendo amanhã — " + taskTitle
                : "Lembrete URGENTE: Tarefa vencendo em 1 hora — " + taskTitle;
        String html = buildReminderHtml(userName, taskTitle, taskDescription, dueDate, priority, reminderType);

        if (!isConfigured()) {
            log.warn("Serviço de email não configurado completamente; a usar modo mock.");
            logMockEmail(toEmail, subject, taskTitle, taskDescription, dueDate, priority, "LEMBRETE");
            return;
        }
        sendHtml(toEmail, subject, html, "lembrete");
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    private void sendHtml(String toEmail, String subject, String html, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email de {} enviado para: {}", type, toEmail);
        } catch (MailException | jakarta.mail.MessagingException e) {
            log.error("Erro ao enviar email de {} para {} [{}]: {}", type, toEmail,
                    e.getClass().getSimpleName(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Falha inesperada ao enviar email de {} para {} [{}]: {}", type, toEmail,
                    e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void logMockEmail(String toEmail, String subject, String taskTitle,
                               String taskDescription, LocalDateTime dueDate,
                               Task.TaskPriority priority, String type) {
        log.info("""

                ╔══════════════════════════════════════════════════════════
                ║  [EMAIL MOCK — {}]
                ║  Para:       {}
                ║  Assunto:    {}
                ║  Título:     {}
                ║  Descrição:  {}
                ║  Vencimento: {}
                ║  Prioridade: {}
                ║
                ║  Para enviar emails reais:
                ║    export GMAIL_EMAIL=seu@gmail.com
                ║    export GMAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx  (App Password)
                ╚══════════════════════════════════════════════════════════
                """,
                type, toEmail, subject, taskTitle,
                taskDescription != null ? taskDescription : "—",
                dueDate.format(FMT), priority);
    }

    // ─── HTML templates ──────────────────────────────────────────────────────

    private String buildCreatedHtml(String userName, String taskTitle, String taskDescription,
                                     LocalDateTime dueDate, Task.TaskPriority priority) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f9f9f9;padding:24px;">
                  <div style="max-width:520px;margin:0 auto;background:#fff;border-left:4px solid #1EC898;padding:24px;border-radius:4px;">
                    <h2 style="margin-top:0;color:#1EC898;">✓ Tarefa Criada</h2>
                    <p>Olá <strong>%s</strong>, sua tarefa foi criada com sucesso.</p>
                    <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                      <tr><td style="padding:6px 0;color:#555;">Título</td><td><strong>%s</strong></td></tr>
                      <tr><td style="padding:6px 0;color:#555;">Descrição</td><td>%s</td></tr>
                      <tr><td style="padding:6px 0;color:#555;">Vencimento</td><td>%s</td></tr>
                      <tr><td style="padding:6px 0;color:#555;">Prioridade</td>
                          <td style="color:%s;font-weight:bold;">%s</td></tr>
                    </table>
                    <p style="color:#666;font-size:12px;">Email automático — Task Scheduler AI</p>
                  </div>
                </body></html>
                """.formatted(userName, taskTitle,
                taskDescription != null ? taskDescription : "—",
                dueDate.format(FMT), priorityColor(priority), priority);
    }

    private String buildReminderHtml(String userName, String taskTitle, String taskDescription,
                                      LocalDateTime dueDate, Task.TaskPriority priority,
                                      ReminderLog.ReminderType type) {
        String timeMsg = type == ReminderLog.ReminderType.DAY_BEFORE ? "amanhã" : "em 1 hora";
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f9f9f9;padding:24px;">
                  <div style="max-width:520px;margin:0 auto;background:#fff;border-left:4px solid %s;padding:24px;border-radius:4px;">
                    <h2 style="margin-top:0;">Lembrete de Tarefa</h2>
                    <p>Olá <strong>%s</strong>, sua tarefa <strong>%s</strong> vence <strong>%s</strong>.</p>
                    <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                      <tr><td style="padding:6px 0;color:#555;">Título</td><td><strong>%s</strong></td></tr>
                      <tr><td style="padding:6px 0;color:#555;">Descrição</td><td>%s</td></tr>
                      <tr><td style="padding:6px 0;color:#555;">Vencimento</td><td>%s</td></tr>
                      <tr><td style="padding:6px 0;color:#555;">Prioridade</td>
                          <td style="color:%s;font-weight:bold;">%s</td></tr>
                    </table>
                    <p style="color:#666;font-size:12px;">Email automático — Task Scheduler AI</p>
                  </div>
                </body></html>
                """.formatted(priorityColor(priority), userName, taskTitle, timeMsg,
                taskTitle, taskDescription != null ? taskDescription : "—",
                dueDate.format(FMT), priorityColor(priority), priority);
    }

    private String priorityColor(Task.TaskPriority priority) {
        return switch (priority) {
            case URGENT -> "#D94040";
            case HIGH   -> "#D4A332";
            case MEDIUM -> "#4A8FE8";
            case LOW    -> "#1EC898";
        };
    }
}
