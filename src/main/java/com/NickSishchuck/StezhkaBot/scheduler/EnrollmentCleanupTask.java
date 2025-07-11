package com.NickSishchuck.StezhkaBot.scheduler;

import com.NickSishchuck.StezhkaBot.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class EnrollmentCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentCleanupTask.class);

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentCleanupTask(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Clean up abandoned enrollment forms every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes in milliseconds
    public void cleanupAbandonedEnrollments() {
        logger.debug("Starting cleanup of abandoned enrollments");
        enrollmentService.cleanupAbandonedEnrollments();
        logger.debug("Completed cleanup of abandoned enrollments");
    }
}