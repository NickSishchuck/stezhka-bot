package com.NickSishchuck.StezhkaBot.scheduler;

import com.NickSishchuck.StezhkaBot.service.AdminStateService;
import com.NickSishchuck.StezhkaBot.service.ConsultationService;
import com.NickSishchuck.StezhkaBot.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class CleanupTasks {

    private static final Logger logger = LoggerFactory.getLogger(CleanupTasks.class);

    private final EnrollmentService enrollmentService;
    private final AdminStateService adminStateService;
    private final ConsultationService consultationService;

    @Autowired
    public CleanupTasks(EnrollmentService enrollmentService, AdminStateService adminStateService,
                        ConsultationService consultationService) {
        this.enrollmentService = enrollmentService;
        this.adminStateService = adminStateService;
        this.consultationService = consultationService;
    }

    /**
     * Clean up abandoned enrollment forms and user request counts every 30 minutes
     * This also handles the rate limiting cleanup (requests older than 30 minutes)
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes in milliseconds
    public void cleanupAbandonedEnrollments() {
        logger.debug("Starting cleanup of abandoned enrollments and request counts");
        enrollmentService.cleanupAbandonedEnrollments();
        logger.debug("Completed cleanup of abandoned enrollments and request counts");
    }

    /**
     * Clean up old admin editing sessions every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes in milliseconds
    public void cleanupOldEditingSessions() {
        logger.debug("Starting cleanup of old admin editing sessions");
        adminStateService.cleanupOldSessions();
        logger.debug("Completed cleanup of old admin editing sessions");
    }

    /**
     * Clean up abandoned consultation forms and user request counts every 30 minutes
     * This also handles the rate limiting cleanup (requests older than 30 minutes)
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes in milliseconds
    public void cleanupAbandonedConsultations() {
        logger.debug("Starting cleanup of abandoned consultations and request counts");
        consultationService.cleanupAbandonedConsultations();
        logger.debug("Completed cleanup of abandoned consultations and request counts");
    }

    /**
     * Additional cleanup every 15 minutes for more aggressive rate limit cleanup
     * This ensures that users don't have to wait too long for their request count to reset
     */
    @Scheduled(fixedDelay = 900000)
    public void cleanupRequestCounts() {
        logger.debug("Starting additional cleanup of request counts");
        enrollmentService.cleanupAbandonedEnrollments();
        consultationService.cleanupAbandonedConsultations();
        logger.debug("Completed additional cleanup of request counts");
    }
}