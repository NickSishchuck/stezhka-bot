package com.NickSishchuck.StezhkaBot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class MenuHandlerRegistry {

    private final List<MenuHandler> handlers;
    private TelegramClient telegramClient;

    @Autowired
    public MenuHandlerRegistry(List<MenuHandler> handlers) {
        this.handlers = handlers;
    }

    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        handlers.forEach(handler -> handler.setTelegramClient(telegramClient));
    }

    public void handle(long chatId, String callbackData) {
        handlers.stream()
                .filter(handler -> handler.canHandle(callbackData))
                .findFirst()
                .ifPresentOrElse(
                        handler -> handler.handle(chatId, callbackData),
                        () -> {
                            // Fallback to main menu if no handler found
                            handlers.stream()
                                    .filter(handler -> handler.canHandle("main"))
                                    .findFirst()
                                    .ifPresent(handler -> handler.handle(chatId, "main"));
                        }
                );
    }
}