package com.NickSishchuck.StezhkaBot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class AdminNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationService.class);

    @Value("${bot.admin.user.ids:}")
    private String adminUserIds;

    private TelegramClient telegramClient;

    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Send startup notification to all admins
     */
    public void sendStartupNotification(String botUsername) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String message = String.format(
                "🟢 Bot Started Successfully!\n\n" +
                        "⏰ Time: %s\n" +
                        "🤖 Bot: %s\n" +
                        "📊 Status: Online and ready\n",
                timestamp, botUsername
        );

        sendToAllAdmins(message, "startup");
    }

    /**
     * Send shutdown notification to all admins
     */
    public void sendShutdownNotification(String botUsername) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String message = String.format(
                "🔴 Bot Going Offline\n\n" +
                        "⏰ Time: %s\n" +
                        "🤖 Bot: %s\n" +
                        "🛠️ Status: Entering maintenance mode\n" +
                        "⚠️ Bot will be temporarily unavailable\n\n" +
                        "Please wait for restart notification.",
                timestamp, botUsername
        );

        sendToAllAdmins(message, "shutdown");
    }

    /**
     * Send error notification to all admins
     */
    public void sendErrorNotification(String errorType, String errorMessage) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String message = String.format(
                "❌ Bot Error Alert\n\n" +
                        "⏰ Time: %s\n" +
                        "🚨 Error Type: %s\n" +
                        "📝 Details: %s\n\n" +
                        "Please check the bot status.",
                timestamp, errorType, errorMessage
        );

        sendToAllAdmins(message, "error");
    }

    /**
     * Send database notification to all admins
     */
    public void sendDatabaseNotification(String action, boolean success) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String status = success ? "✅ Success" : "❌ Failed";
        String emoji = success ? "🗄️" : "⚠️";

        String message = String.format(
                "%s Database %s\n\n" +
                        "⏰ Time: %s\n" +
                        "🔧 Action: %s\n" +
                        "📊 Result: %s",
                emoji, action, timestamp, action, status
        );

        sendToAllAdmins(message, "database");
    }

    /**
     * Send custom notification to all admins
     */
    public void sendCustomNotification(String title, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String message = String.format(
                "📢 %s\n\n" +
                        "⏰ Time: %s\n" +
                        "📝 Details: %s",
                title, timestamp, details
        );

        sendToAllAdmins(message, "custom");
    }

    /**
     * Send message to all admin users
     */
    private void sendToAllAdmins(String message, String type) {
        if (telegramClient == null) {
            logger.warn("TelegramClient not available for {} notification", type);
            return;
        }

        List<Long> adminIds = getAdminUserIds();
        if (adminIds.isEmpty()) {
            logger.info("No admin users configured for {} notifications", type);
            return;
        }

        for (Long adminId : adminIds) {
            try {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(adminId)
                        .text(message)
                        .build();

                telegramClient.execute(sendMessage);
                logger.info("Sent {} notification to admin {}", type, adminId);

                // Small delay to avoid rate limiting
                Thread.sleep(100);

            } catch (Exception e) {
                logger.error("Failed to send {} notification to admin {}: {}", type, adminId, e.getMessage());
            }
        }
    }

    /**
     * Parse admin user IDs from configuration
     */
    private List<Long> getAdminUserIds() {
        if (adminUserIds == null || adminUserIds.trim().isEmpty()) {
            return List.of();
        }

        try {
            return Arrays.stream(adminUserIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException e) {
            logger.error("Invalid admin user ID format: {}", adminUserIds, e);
            return List.of();
        }
    }

    /**
     * Check if admin notifications are configured
     */
    public boolean areAdminNotificationsConfigured() {
        return !getAdminUserIds().isEmpty() && telegramClient != null;
    }
}