package com.NickSishchuck.StezhkaBot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;


@Service
public class EchoBotService implements LongPollingUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EchoBotService.class);

    private final String botUsername;
    private TelegramClient telegramClient;

    @Autowired
    public EchoBotService(String botUsername) {
        this.botUsername = botUsername;
    }

    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            }
        }
    }

    private void handleTextMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();
        String firstName = message.getFrom().getFirstName();

        logger.info("Received message from {}: {}", firstName, messageText);

        String responseText;
        if (messageText.equals("/start")) {
            responseText = "Hello " + firstName + "! I'm an echo bot. Send me any message and I'll echo it back!";
        } else {
            responseText = messageText;
        }

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(responseText)
                .build();

        try {
            telegramClient.execute(sendMessage);
            logger.info("Message sent successfully to {}", firstName);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }

    public String getBotUsername() {
        return botUsername;
    }
}