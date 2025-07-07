package com.NickSishchuck.StezhkaBot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
}