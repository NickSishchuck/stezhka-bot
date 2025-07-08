package com.NickSishchuck.StezhkaBot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final TelegramClient telegramClient;

    public MessageSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                //.parseMode("MarkdownV2")
                .build();

        try {
            telegramClient.execute(message);
            logger.info("Message sent successfully to chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat {}", chatId, e);
        }
    }

    public void sendPlainMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                // No parseMode - sends as plain text, no MarkdownV2 parsing
                .build();

        try {
            telegramClient.execute(message);
            logger.info("Plain message sent successfully to chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send plain message to chat {}", chatId, e);
        }
    }

    public void sendMarkdownMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("MarkdownV2")
                .build();

        try {
            telegramClient.execute(message);
            logger.info("Markdown message sent successfully to chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send markdown message to chat {}", chatId, e);
        }
    }

    // New methods for editing messages
    public void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                //.parseMode("MarkdownV2")
                .build();

        try {
            telegramClient.execute(editMessage);
            logger.info("Message edited successfully in chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit message in chat {}: {}", chatId, e.getMessage());
            // Fallback: send new message if editing fails
            sendMessage(chatId, text, keyboard);
        }
    }

    public void editPlainMessage(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                // No parseMode - edits as plain text
                .build();

        try {
            telegramClient.execute(editMessage);
            logger.info("Plain message edited successfully in chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit plain message in chat {}: {}", chatId, e.getMessage());
            // Fallback: send new message if editing fails
            sendPlainMessage(chatId, text, keyboard);
        }
    }

    public void editMarkdownMessage(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("MarkdownV2")
                .build();

        try {
            telegramClient.execute(editMessage);
            logger.info("Markdown message edited successfully in chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit markdown message in chat {}: {}", chatId, e.getMessage());
            // Fallback: send new message if editing fails
            sendMarkdownMessage(chatId, text, keyboard);
        }
    }

    public void editKeyboard(long chatId, int messageId, InlineKeyboardMarkup keyboard) {
        EditMessageReplyMarkup editKeyboard = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(editKeyboard);
            logger.info("Keyboard edited successfully in chat {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit keyboard in chat {}: {}", chatId, e.getMessage());
        }
    }
}