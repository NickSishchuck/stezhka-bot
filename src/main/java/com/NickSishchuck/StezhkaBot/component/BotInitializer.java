package com.NickSishchuck.StezhkaBot.component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import com.NickSishchuck.StezhkaBot.service.StezhkaBotService;
import com.NickSishchuck.StezhkaBot.service.AdminNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Component
public class BotInitializer {

    private static final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

    private final String botToken;
    private final StezhkaBotService stezhkaBotService;
    private final AdminNotificationService notificationService;
    private TelegramBotsLongPollingApplication botsApplication;
    private TelegramClient telegramClient;

    @Autowired
    public BotInitializer(String botToken, StezhkaBotService stezhkaBotService, AdminNotificationService notificationService) {
        this.botToken = botToken;
        this.stezhkaBotService = stezhkaBotService;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Telegram bot...");

            // Create Telegram client
            telegramClient = new OkHttpTelegramClient(botToken);

            // Set the client in the bot service
            stezhkaBotService.setTelegramClient(telegramClient);
            notificationService.setTelegramClient(telegramClient);

            // Create and start the long polling application
            botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, stezhkaBotService);

            logger.info("Telegram bot started successfully!");
            logger.info("Bot username: {}", stezhkaBotService.getBotUsername());

            notificationService.sendStartupNotification(stezhkaBotService.getBotUsername());

        } catch (TelegramApiException e) {
            logger.error("Failed to initialize Telegram bot", e);
            notificationService.sendErrorNotification("Bot Startup Failed", e.getMessage());
            throw new RuntimeException("Failed to initialize Telegram bot", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Bot shutdown initialized...");
        notificationService.sendShutdownNotification(stezhkaBotService.getBotUsername());

        //Making sure the message is sent
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (botsApplication != null) {
            try {
                logger.info("Shutting down Telegram bot...");
                botsApplication.close();
                logger.info("Telegram bot shutdown completed");
            } catch (Exception e) {
                logger.error("Error during bot shutdown", e);
            }
        }
    }
}