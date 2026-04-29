package com.taskscheduler.job;

import com.taskscheduler.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReminderJob implements Job {
    private static ReminderService reminderService;

    @Autowired
    public void setReminderService(ReminderService reminderService) {
        ReminderJob.reminderService = reminderService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Executing ReminderJob");
            reminderService.checkAndSendReminders();
        } catch (Exception e) {
            log.error("Error executing ReminderJob", e);
            throw new JobExecutionException(e);
        }
    }
}
