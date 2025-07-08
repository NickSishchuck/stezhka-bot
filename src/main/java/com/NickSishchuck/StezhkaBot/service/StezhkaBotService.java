package com.NickSishchuck.StezhkaBot.service;

import com.NickSishchuck.StezhkaBot.handler.AdminHandler;
import com.NickSishchuck.StezhkaBot.handler.MenuHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
public class StezhkaBotService implements LongPollingUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(StezhkaBotService.class);
    private final AdminHandler adminHandler;

    private final String botUsername;
    private final MenuHandlerRegistry handlerRegistry;
    private TelegramClient telegramClient;

    @Autowired
    public StezhkaBotService(String botUsername, MenuHandlerRegistry handlerRegistry, AdminHandler adminHandler) {
        this.botUsername = botUsername;
        this.handlerRegistry = handlerRegistry;
        this.adminHandler = adminHandler;
    }

    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        handlerRegistry.setTelegramClient(telegramClient);
        adminHandler.setTelegramClient(telegramClient);
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            try {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    handleTextMessage(update.getMessage());
                } else if (update.hasCallbackQuery()) {
                    handleCallbackQuery(update);
                }
            } catch (Exception e) {
                logger.error("Error processing update", e);
            }
        }
    }

    private void handleTextMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();
        String firstName = message.getFrom().getFirstName();

        logger.info("Received message from {}: {}", firstName, messageText);

        // Handle /start and any other text messages
        if (messageText.equals("/start")) {
            handlerRegistry.handle(chatId, "start");
            return; // Important: return here to prevent falling through to the else block
        }

        if (messageText.startsWith("/admin")) {
            adminHandler.handle(chatId, "/admin");
            return;
        }

        if (messageText.startsWith("/update_text ")) {
            boolean handled = adminHandler.handleTextUpdate(chatId, messageText);
            if (handled) return;
        } else {
            // TODO For now, just redirect unknown messages to main menu
            handlerRegistry.handle(chatId, "main");
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String firstName = update.getCallbackQuery().getFrom().getFirstName();

        logger.info("Received callback from {}: {}", firstName, callbackData);

        // Handle admin callbacks
        if (adminHandler.canHandle(callbackData)) {
            adminHandler.handle(chatId, messageId, callbackData);
            // Answer callback query to remove loading indicator
            try {
                telegramClient.execute(org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.builder()
                        .callbackQueryId(update.getCallbackQuery().getId())
                        .build());
            } catch (Exception e) {
                logger.warn("Failed to answer callback query", e);
            }
            return;
        }

        // Answer callback query to remove loading indicator
        try {
            telegramClient.execute(org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .build());
        } catch (Exception e) {
            logger.warn("Failed to answer callback query", e);
        }

        // Delegate to appropriate handler using edit method for callback queries
        handlerRegistry.handle(chatId, messageId, callbackData);
    }

    public String getBotUsername() {
        return botUsername;
    }
}