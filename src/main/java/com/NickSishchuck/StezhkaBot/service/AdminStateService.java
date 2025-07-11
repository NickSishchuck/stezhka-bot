package com.NickSishchuck.StezhkaBot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminStateService {

    private static final Logger logger = LoggerFactory.getLogger(AdminStateService.class);

    // Store active editing sessions
    private final Map<Long, EditingState> activeEditingSessions = new ConcurrentHashMap<>();

    /**
     * Start text editing session
     */
    public void startEditing(Long adminId, String textKey, String currentValue) {
        EditingState state = new EditingState(textKey, currentValue);
        activeEditingSessions.put(adminId, state);
        logger.info("Admin {} started editing text key: {}", adminId, textKey);
    }

    /**
     * Get editing state for admin
     */
    public EditingState getEditingState(Long adminId) {
        return activeEditingSessions.get(adminId);
    }

    /**
     * Clear editing state
     */
    public void clearEditingState(Long adminId) {
        EditingState removed = activeEditingSessions.remove(adminId);
        if (removed != null) {
            logger.info("Cleared editing state for admin {}, was editing: {}", adminId, removed.getTextKey());
        }
    }

    /**
     * Check if admin is in editing mode
     */
    public boolean isEditing(Long adminId) {
        return activeEditingSessions.containsKey(adminId);
    }

    /**
     * Clean up old editing sessions (called periodically)
     */
    public void cleanupOldSessions() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        activeEditingSessions.entrySet().removeIf(entry ->
                entry.getValue().getStartedAt().isBefore(timeout)
        );
    }

    /**
     * Editing state class
     */
    public static class EditingState {
        private final String textKey;
        private final String previousValue;
        private final LocalDateTime startedAt;

        public EditingState(String textKey, String previousValue) {
            this.textKey = textKey;
            this.previousValue = previousValue;
            this.startedAt = LocalDateTime.now();
        }

        public String getTextKey() { return textKey; }
        public String getPreviousValue() { return previousValue; }
        public LocalDateTime getStartedAt() { return startedAt; }
    }
}