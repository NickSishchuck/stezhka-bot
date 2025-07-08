package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.constants.MenuTexts;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StaticContentHandler implements MenuHandler {

    private TelegramClient telegramClient;
    private MessageSender messageSender;
    private final MenuTexts menuTexts;

    @Autowired
    public StaticContentHandler(MenuTexts menuTexts) {
        this.menuTexts = menuTexts;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.equals("faq_show") ||
                callbackData.equals("contacts_show") ||
                callbackData.equals("news_show");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        switch (callbackData) {
            case "faq_show" -> showFAQ(chatId);
            case "contacts_show" -> showContacts(chatId);
            case "news_show" -> showNews(chatId);
        }
    }

    @Override
    public void handle(long chatId, int messageId, String callbackData) {
        switch (callbackData) {
            case "faq_show" -> editFAQ(chatId, messageId);
            case "contacts_show" -> editContacts(chatId, messageId);
            case "news_show" -> editNews(chatId, messageId);
        }
    }

    private void showFAQ(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getFaqText(), keyboard);
    }

    private void editFAQ(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getFaqText(), keyboard);
    }

    private void showContacts(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getContactsText(), keyboard);
    }

    private void editContacts(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getContactsText(), keyboard);
    }

    private void showNews(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getNewsText(), keyboard);
    }

    private void editNews(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getNewsText(), keyboard);
    }
}