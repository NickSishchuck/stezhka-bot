package com.NickSishchuck.StezhkaBot.handler;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface MenuHandler {
    void handle(long chatId, String callbackData);
    void handle(long chatId, int messageId, String callbackData);
    boolean canHandle(String callbackData);
    void setTelegramClient(TelegramClient telegramClient);
}