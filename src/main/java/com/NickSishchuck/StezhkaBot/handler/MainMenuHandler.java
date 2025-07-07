package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.constants.MenuTexts;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class MainMenuHandler implements MenuHandler {

    private TelegramClient telegramClient;
    private MessageSender messageSender;

    private final MenuTexts menuTexts;

    @Autowired
    public MainMenuHandler(MenuTexts menuTexts) {
        this.menuTexts = menuTexts;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.equals("start") ||
                callbackData.equals("main") ||
                callbackData.equals("back_main");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        switch (callbackData) {
            case "start" -> showWelcomeMessage(chatId);
            case "main", "back_main" -> showMainMenu(chatId);
        }
    }

    private void showWelcomeMessage(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🚀 Почати", "main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getWelcomeMessage(), keyboard);
    }

    private void showMainMenu(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🎓 Навчальні програми", "programs_main")
                .addButton("📞 Записатися на консультацію NO HANDLER", "consultations_main")
                .addRow()
                .addButton("❓ Часті запитання", "faq_show")
                .addButton("📋 Контакти та адреса", "contacts_show")
                .addRow()
                .addButton("📢 Новини та акції", "news_show")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getMainMenuMessage(), keyboard);
    }
}
