package com.NickSishchuck.StezhkaBot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class MenuBuilder {
    private final List<InlineKeyboardRow> rows = new ArrayList<>();
    private InlineKeyboardRow currentRow = new InlineKeyboardRow();

    public MenuBuilder addButton(String text, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();

        currentRow.add(button);
        return this;
    }

    public MenuBuilder addRow() {
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
            currentRow = new InlineKeyboardRow();
        }
        return this;
    }

    public InlineKeyboardMarkup build() {
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}