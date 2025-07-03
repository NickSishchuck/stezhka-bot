
package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.constants.MenuTexts;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StaticContentHandler implements MenuHandler {

    private TelegramClient telegramClient;
    private MessageSender messageSender;

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

    private void showFAQ(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, MenuTexts.FAQ_TEXT, keyboard);
    }

    private void showContacts(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, MenuTexts.CONTACTS_TEXT, keyboard);
    }

    private void showNews(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, MenuTexts.NEWS_TEXT, keyboard);
    }
}
