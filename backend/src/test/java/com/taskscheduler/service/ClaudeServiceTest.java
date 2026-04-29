package com.taskscheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaudeServiceTest {
    private final ClaudeService service = new ClaudeService(null, null, new ObjectMapper());

    @Test
    void parsesPortugueseWeekdayForUpcomingFriday() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 29, 15, 0);

        LocalDateTime result = service.extractDateFromMessage("Estudar componentes react para sexta às 9h", reference);

        assertEquals(LocalDateTime.of(2026, 5, 1, 9, 0), result);
    }

    @Test
    void parsesPortugueseWeekdayFromSaturdayToNextMonday() {
        LocalDateTime reference = LocalDateTime.of(2026, 5, 2, 9, 0);

        LocalDateTime result = service.extractDateFromMessage("reunião segunda-feira às 14h", reference);

        assertEquals(LocalDateTime.of(2026, 5, 4, 14, 0), result);
    }

    @Test
    void keepsSameDayWhenWeekdayTimeHasNotPassed() {
        LocalDateTime reference = LocalDateTime.of(2026, 5, 1, 8, 0);

        LocalDateTime result = service.extractDateFromMessage("sexta às 9h", reference);

        assertEquals(LocalDateTime.of(2026, 5, 1, 9, 0), result);
    }

    @Test
    void rollsWeekdayToFollowingWeekWhenTimeHasPassed() {
        LocalDateTime reference = LocalDateTime.of(2026, 5, 1, 10, 0);

        LocalDateTime result = service.extractDateFromMessage("sexta às 9h", reference);

        assertEquals(LocalDateTime.of(2026, 5, 8, 9, 0), result);
    }

    @Test
    void nextWeekdayForcesFollowingWeekWhenSameDay() {
        LocalDateTime reference = LocalDateTime.of(2026, 5, 1, 8, 0);

        LocalDateTime result = service.extractDateFromMessage("próxima sexta às 9h", reference);

        assertEquals(LocalDateTime.of(2026, 5, 8, 9, 0), result);
    }

    @Test
    void parsesEnglishWeekdayAndTime() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 29, 15, 0);

        LocalDateTime result = service.extractDateFromMessage("Study React components Friday at 9", reference);

        assertEquals(LocalDateTime.of(2026, 5, 1, 9, 0), result);
    }

    @Test
    void parsesEnglishNextWeekday() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 29, 15, 0);

        LocalDateTime result = service.extractDateFromMessage("next Tuesday at 18:30", reference);

        assertEquals(LocalDateTime.of(2026, 5, 5, 18, 30), result);
    }

    @Test
    void parsesTodayWithHourSuffix() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 29, 8, 30);

        LocalDateTime result = service.extractDateFromMessage("today at 16h", reference);

        assertEquals(LocalDateTime.of(2026, 4, 29, 16, 0), result);
    }

    @Test
    void keepsTomorrowRegressionWorking() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 29, 8, 30);

        LocalDateTime result = service.extractDateFromMessage("amanhã às 10h", reference);

        assertEquals(LocalDateTime.of(2026, 4, 30, 10, 0), result);
    }

    @Test
    void parsesAbsoluteDateInPortugueseWithoutTreatingDayAsHour() {
        LocalDateTime reference = LocalDateTime.of(2026, 4, 29, 8, 30);

        LocalDateTime result = service.extractDateFromMessage("12 de maio às 11h", reference);

        assertEquals(LocalDateTime.of(2026, 5, 12, 11, 0), result);
    }

    @Test
    void infersLowPriorityAndKeepsDescription() {
        String normalized = service.normalize("Study React components Friday at 9 low priority");

        assertEquals("LOW", service.extractPriority(normalized));
        assertEquals("Study React components Friday at 9 low priority",
                service.buildDescription("Study React components Friday at 9 low priority", "Study React components"));
    }

    @Test
    void stripsTemporalFragmentFromTitleInEnglishAndPortuguese() {
        assertEquals("Estudar componentes react",
                service.buildTitle("Estudar componentes react para sexta às 9h", service.normalize("Estudar componentes react para sexta às 9h")));
        assertEquals("Study React components",
                service.buildTitle("Study React components Friday at 9", service.normalize("Study React components Friday at 9")));
    }
}
