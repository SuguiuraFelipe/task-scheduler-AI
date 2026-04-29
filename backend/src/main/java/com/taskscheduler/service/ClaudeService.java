package com.taskscheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskscheduler.dto.AITaskSuggestion;
import com.taskscheduler.entity.AiConversation;
import com.taskscheduler.entity.User;
import com.taskscheduler.repository.AiConversationRepository;
import com.taskscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeService {
    private static final Pattern PT_ABSOLUTE_DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})\\s+de\\s+(janeiro|fevereiro|marco|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)\\b");
    private static final Pattern EN_ABSOLUTE_DATE_PATTERN = Pattern.compile(
            "\\b(january|february|march|april|may|june|july|august|september|october|november|december)\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b");
    private static final Pattern EN_ABSOLUTE_DATE_REVERSED_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?(january|february|march|april|may|june|july|august|september|october|november|december)\\b");
    private static final Pattern TITLE_TIME_PATTERN = Pattern.compile(
            "(?i)\\s+(?:a[sà]|at)\\s+\\d{1,2}(?::\\d{2})?\\s*(?:am|pm|h|horas?|hrs?)?\\b");
    private static final Pattern TITLE_PRIORITY_PATTERN = Pattern.compile(
            "(?i)\\s+(?:com\\s+)?prioridade\\s+\\w+\\b|\\s+\\b(?:urgent|urgente|alta|alto|baixo|baixa|m[eé]dia|low|medium|high)\\b");
    private static final Map<DayOfWeek, String[]> WEEKDAY_TERMS = new LinkedHashMap<>() {{
        put(DayOfWeek.MONDAY, new String[]{"segunda", "segunda feira", "monday"});
        put(DayOfWeek.TUESDAY, new String[]{"terca", "terca feira", "tuesday"});
        put(DayOfWeek.WEDNESDAY, new String[]{"quarta", "quarta feira", "wednesday"});
        put(DayOfWeek.THURSDAY, new String[]{"quinta", "quinta feira", "thursday"});
        put(DayOfWeek.FRIDAY, new String[]{"sexta", "sexta feira", "friday"});
        put(DayOfWeek.SATURDAY, new String[]{"sabado", "saturday"});
        put(DayOfWeek.SUNDAY, new String[]{"domingo", "sunday"});
    }};
    private static final Map<String, Month> MONTHS = new LinkedHashMap<>() {{
        put("janeiro", Month.JANUARY);
        put("january", Month.JANUARY);
        put("fevereiro", Month.FEBRUARY);
        put("february", Month.FEBRUARY);
        put("marco", Month.MARCH);
        put("march", Month.MARCH);
        put("abril", Month.APRIL);
        put("april", Month.APRIL);
        put("maio", Month.MAY);
        put("may", Month.MAY);
        put("junho", Month.JUNE);
        put("june", Month.JUNE);
        put("julho", Month.JULY);
        put("july", Month.JULY);
        put("agosto", Month.AUGUST);
        put("august", Month.AUGUST);
        put("setembro", Month.SEPTEMBER);
        put("september", Month.SEPTEMBER);
        put("outubro", Month.OCTOBER);
        put("october", Month.OCTOBER);
        put("novembro", Month.NOVEMBER);
        put("november", Month.NOVEMBER);
        put("dezembro", Month.DECEMBER);
        put("december", Month.DECEMBER);
    }};

    private final AiConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AITaskSuggestion suggestTask(String userMessage, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado: " + userEmail));

        String mockResponse = generateMockResponse(userMessage);

        AiConversation conversation = new AiConversation();
        conversation.setUser(user);
        conversation.setUserMessage(userMessage);
        conversation.setAssistantResponse(mockResponse);
        conversationRepository.save(conversation);

        return parseMockResponse(mockResponse);
    }

    private String generateMockResponse(String userMessage) {
        String normalizedMessage = normalize(userMessage);
        LocalDateTime dueDate = extractDateFromMessage(userMessage);
        String priority = extractPriority(normalizedMessage);
        String title = buildTitle(userMessage, normalizedMessage);
        String description = buildDescription(userMessage, title);

        try {
            return objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
                put("title", title);
                put("description", description);
                put("dueDate", dueDate.toString());
                put("priority", priority);
            }});
        } catch (Exception e) {
            log.error("Error generating mock response", e);
            return "{}";
        }
    }

    private LocalDateTime extractDateFromMessage(String message) {
        return extractDateFromMessage(message, LocalDateTime.now());
    }

    LocalDateTime extractDateFromMessage(String message, LocalDateTime reference) {
        String normalizedMessage = normalize(message);
        ParsedTime time = extractTime(normalizedMessage);

        LocalDateTime absoluteDate = extractAbsoluteDate(normalizedMessage, reference, time);
        if (absoluteDate != null) {
            return absoluteDate;
        }

        LocalDateTime relativeDate = extractRelativeDate(normalizedMessage, reference, time);
        if (relativeDate != null) {
            return relativeDate;
        }

        LocalDateTime weekdayDate = extractWeekdayDate(normalizedMessage, reference, time);
        if (weekdayDate != null) {
            return weekdayDate;
        }

        return applyTime(reference.plusDays(1), time);
    }

    String extractPriority(String normalizedMessage) {
        if (containsAny(normalizedMessage,
                "prioridade baixa", "baixa prioridade", "pouco urgente", "sem urgencia",
                "sem urgência", "baixa", "low")) {
            return "LOW";
        }

        if (containsAny(normalizedMessage,
                "prioridade urgente", "muito urgente", "urgente", "urgentissima",
                "urgentissima", "critical", "critica", "crítica")) {
            return "URGENT";
        }

        if (containsAny(normalizedMessage,
                "prioridade alta", "alta prioridade", "importante", "alta", "high")) {
            return "HIGH";
        }

        if (containsAny(normalizedMessage,
                "prioridade media", "prioridade média", "media prioridade",
                "média prioridade", "moderada", "medium")) {
            return "MEDIUM";
        }

        if (containsAny(normalizedMessage, "estud", "prova", "exame", "apresentacao", "apresentação")) {
            return "HIGH";
        }

        if (containsAny(normalizedMessage, "reuniao", "reunião", "prazo hoje", "ate hoje", "até hoje")) {
            return "URGENT";
        }

        return "MEDIUM";
    }

    String buildTitle(String originalMessage, String normalizedMessage) {
        String cleaned = originalMessage.trim().replaceAll("\\s+", " ");

        cleaned = cleaned.replaceFirst("(?i)^\\s*(preciso|tenho que|tenho de|quero|lembrar de|lembrar-me de)\\s+", "");
        cleaned = cleaned.replaceFirst("(?i)^\\s*(por favor\\s+)?(criar|adicione?|adicionar|agenda[r]?|marque?)\\s+(uma\\s+)?tarefa\\s+(para\\s+)?", "");

        String[] splitters = {
                "(?i)\\s+(amanh[aã]|tomorrow|hoje|today|depois de amanh[aã]|day after tomorrow|pr[oó]xima\\s+semana|next\\s+week|fim de semana|weekend|pr[oó]xim[ao]?\\s+(?:segunda|ter[cç]a|quarta|quinta|sexta|s[aá]bado|domingo)(?:-feira)?|next\\s+(?:monday|tuesday|wednesday|thursday|friday|saturday|sunday)|segunda(?:-feira)?|ter[cç]a(?:-feira)?|quarta(?:-feira)?|quinta(?:-feira)?|sexta(?:-feira)?|s[aá]bado|domingo|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b",
                "(?i)\\s+\\d{1,2}\\s+de\\s+(?:janeiro|fevereiro|mar[cç]o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)\\b",
                "(?i)\\s+(?:january|february|march|april|may|june|july|august|september|october|november|december)\\s+\\d{1,2}(?:st|nd|rd|th)?\\b"
        };

        for (String splitter : splitters) {
            cleaned = cleaned.split(splitter, 2)[0].trim();
        }

        cleaned = TITLE_TIME_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = TITLE_PRIORITY_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.replaceAll("\\s+(?:para|by)\\s*$", "");
        cleaned = cleaned.replaceAll("[,;:\\-]+\\s*$", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        if (cleaned.isBlank()) {
            cleaned = fallbackTitleFromKeywords(normalizedMessage);
        }

        if (cleaned.length() > 60) {
            cleaned = truncate(cleaned, 60);
        }

        return capitalize(cleaned);
    }

    String buildDescription(String originalMessage, String title) {
        String cleaned = originalMessage.trim().replaceAll("\\s+", " ");
        if (cleaned.equalsIgnoreCase(title)) {
            return "";
        }
        return cleaned;
    }

    private String fallbackTitleFromKeywords(String normalizedMessage) {
        if (containsAny(normalizedMessage, "comprar")) return "Comprar";
        if (containsAny(normalizedMessage, "estud")) return "Estudar";
        if (containsAny(normalizedMessage, "reuniao", "reunião")) return "Reunião";
        if (containsAny(normalizedMessage, "pagar")) return "Pagar conta";
        if (containsAny(normalizedMessage, "ligar")) return "Fazer ligação";
        if (containsAny(normalizedMessage, "enviar")) return "Enviar pendência";
        return "Nova tarefa";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(normalize(term))) {
                return true;
            }
        }
        return false;
    }

    String normalize(String text) {
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('-', ' ')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "Nova tarefa";
        }
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    private LocalDateTime extractAbsoluteDate(String normalizedMessage, LocalDateTime reference, ParsedTime time) {
        try {
            Matcher ptMatcher = PT_ABSOLUTE_DATE_PATTERN.matcher(normalizedMessage);
            if (ptMatcher.find()) {
                return resolveAbsoluteDate(reference, time, Integer.parseInt(ptMatcher.group(1)), ptMatcher.group(2));
            }

            Matcher enMatcher = EN_ABSOLUTE_DATE_PATTERN.matcher(normalizedMessage);
            if (enMatcher.find()) {
                return resolveAbsoluteDate(reference, time, Integer.parseInt(enMatcher.group(2)), enMatcher.group(1));
            }

            Matcher reversedEnMatcher = EN_ABSOLUTE_DATE_REVERSED_PATTERN.matcher(normalizedMessage);
            if (reversedEnMatcher.find()) {
                return resolveAbsoluteDate(reference, time, Integer.parseInt(reversedEnMatcher.group(1)), reversedEnMatcher.group(2));
            }
        } catch (Exception e) {
            log.warn("Error parsing absolute date from message", e);
        }
        return null;
    }

    private LocalDateTime resolveAbsoluteDate(LocalDateTime reference, ParsedTime time, int day, String monthName) {
        Month month = MONTHS.get(monthName);
        if (month == null) {
            return null;
        }

        LocalDateTime candidate = LocalDateTime.of(reference.getYear(), month, day, time.hour(), time.minute());
        if (candidate.isBefore(reference)) {
            candidate = candidate.plusYears(1);
        }
        return normalizeSeconds(candidate);
    }

    private LocalDateTime extractRelativeDate(String normalizedMessage, LocalDateTime reference, ParsedTime time) {
        if (containsAny(normalizedMessage, "depois de amanha", "day after tomorrow")) {
            return applyTime(reference.plusDays(2), time);
        }
        if (containsAny(normalizedMessage, "amanha", "tomorrow")) {
            return applyTime(reference.plusDays(1), time);
        }
        if (containsAny(normalizedMessage, "hoje", "today")) {
            return applyTime(reference, time);
        }
        if (containsAny(normalizedMessage, "proxima semana", "next week")) {
            return applyTime(reference.plusWeeks(1), time);
        }
        if (containsAny(normalizedMessage, "fim de semana", "weekend")) {
            int daysUntilSaturday = (DayOfWeek.SATURDAY.getValue() - reference.getDayOfWeek().getValue() + 7) % 7;
            if (daysUntilSaturday == 0 && applyTime(reference, time).isBefore(reference)) {
                daysUntilSaturday = 7;
            } else if (daysUntilSaturday == 0 && reference.getDayOfWeek() == DayOfWeek.SATURDAY) {
                return applyTime(reference, time);
            } else if (daysUntilSaturday == 0) {
                daysUntilSaturday = 7;
            }
            return applyTime(reference.plusDays(daysUntilSaturday), time);
        }
        return null;
    }

    private LocalDateTime extractWeekdayDate(String normalizedMessage, LocalDateTime reference, ParsedTime time) {
        for (Map.Entry<DayOfWeek, String[]> entry : WEEKDAY_TERMS.entrySet()) {
            for (String term : entry.getValue()) {
                if (containsTerm(normalizedMessage, term)) {
                    boolean forceNextWeek = containsTerm(normalizedMessage, "proxima " + term)
                            || containsTerm(normalizedMessage, "proximo " + term)
                            || containsTerm(normalizedMessage, "next " + term);
                    return nextOccurrence(reference, entry.getKey(), time.hour(), time.minute(), forceNextWeek);
                }
            }
        }
        return null;
    }

    private LocalDateTime nextOccurrence(LocalDateTime now, DayOfWeek targetDay, int hour, int minute, boolean forceNextWeek) {
        int daysUntil = (targetDay.getValue() - now.getDayOfWeek().getValue() + 7) % 7;
        LocalDateTime candidate = now.plusDays(daysUntil).withHour(hour).withMinute(minute).withSecond(0).withNano(0);

        if (forceNextWeek && daysUntil == 0) {
            return candidate.plusWeeks(1);
        }

        if (candidate.isBefore(now)) {
            return candidate.plusWeeks(1);
        }

        return candidate;
    }

    private ParsedTime extractTime(String normalizedMessage) {
        Pattern[] patterns = new Pattern[]{
                Pattern.compile("\\b(?:as|at)\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm|h|horas?|hrs?)?\\b"),
                Pattern.compile("\\b(\\d{1,2})h(?:(\\d{2}))?\\b"),
                Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\s*(am|pm)?\\b"),
                Pattern.compile("\\b(\\d{1,2})\\s*(am|pm)\\b")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(normalizedMessage);
            if (matcher.find()) {
                try {
                    int hour = Integer.parseInt(matcher.group(1));
                    int minute = matcher.groupCount() >= 2 && matcher.group(2) != null && matcher.group(2).matches("\\d{2}")
                            ? Integer.parseInt(matcher.group(2))
                            : 0;
                    String meridiem = matcher.groupCount() >= 3 ? matcher.group(3) : null;
                    if (meridiem == null && matcher.groupCount() >= 2 && matcher.group(2) != null
                            && ("am".equals(matcher.group(2)) || "pm".equals(matcher.group(2)))) {
                        meridiem = matcher.group(2);
                    }
                    if (meridiem != null) {
                        if ("pm".equals(meridiem) && hour < 12) {
                            hour += 12;
                        }
                        if ("am".equals(meridiem) && hour == 12) {
                            hour = 0;
                        }
                    }
                    if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                        return new ParsedTime(hour, minute);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing time from message", e);
                }
            }
        }

        return new ParsedTime(10, 0);
    }

    private LocalDateTime applyTime(LocalDateTime dateTime, ParsedTime time) {
        return normalizeSeconds(dateTime.withHour(time.hour()).withMinute(time.minute()));
    }

    private LocalDateTime normalizeSeconds(LocalDateTime dateTime) {
        return dateTime.withSecond(0).withNano(0);
    }

    private boolean containsTerm(String normalizedMessage, String normalizedTerm) {
        return Pattern.compile("\\b" + Pattern.quote(normalizedTerm) + "\\b").matcher(normalizedMessage).find();
    }

    private String truncate(String str, int maxLength) {
        return str.length() > maxLength ? str.substring(0, maxLength).trim() + "..." : str;
    }

    private record ParsedTime(int hour, int minute) {
    }

    private AITaskSuggestion parseMockResponse(String response) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(response);
            String dueDateStr = node.get("dueDate").asText();
            LocalDateTime dueDate = dueDateStr.isEmpty() ? LocalDateTime.now().plusDays(1)
                    : LocalDateTime.parse(dueDateStr);

            return AITaskSuggestion.builder()
                    .suggestedTitle(node.get("title").asText("Sem título"))
                    .suggestedDescription(node.get("description").asText(""))
                    .suggestedDueDate(dueDate)
                    .suggestedPriority(node.get("priority").asText("MEDIUM"))
                    .rawResponse(response)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing mock response", e);
            return AITaskSuggestion.builder()
                    .suggestedTitle("Sem título")
                    .suggestedDescription("Não foi possível interpretar a resposta")
                    .suggestedPriority("MEDIUM")
                    .rawResponse(response)
                    .build();
        }
    }
}
