
package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.constants.MenuTexts;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class ProgramsMenuHandler implements MenuHandler {

    private TelegramClient telegramClient;
    private MessageSender messageSender;
    private final MenuTexts menuTexts;

    @Autowired
    public ProgramsMenuHandler(MenuTexts menuTexts) {
        this.menuTexts = menuTexts;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("programs_") ||
                callbackData.startsWith("age_") ||
                callbackData.startsWith("program_");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        switch (callbackData) {
            case "programs_main" -> showProgramsMenu(chatId);
            case "age_4_6" -> showAge4to6Programs(chatId);
            case "age_6_10" -> showAge6to10Programs(chatId);
            case "age_11_15" -> showAge11to15Programs(chatId);
            case "age_15_18" -> showAge15to18Programs(chatId);
            case "age_specialists" -> showSpecialistsPrograms(chatId);
            case "program_preschool" -> showProgramDetails(chatId, "preschool");
            case "program_primary" -> showProgramDetails(chatId, "primary");
            case "program_english" -> showProgramDetails(chatId, "english");
            // Add other program cases...
        }
    }

    private void showProgramsMenu(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Дошкільнята (4-6 років)", "age_4_6")
                .addButton("🎒 Початкова школа (6-10 років)", "age_6_10")
                .addRow()
                .addButton("🧠 Середня школа (11-15 років)", "age_11_15")
                .addButton("🎯 Старша школа (15-18 років)", "age_15_18")
                .addRow()
                .addButton("👨‍⚕️ Спеціалісти", "age_specialists")
                .addRow()
                .addButton("⬅️ Назад", "back_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getProgramsMenuMessage(), keyboard);
    }

    private void showAge4to6Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Підготовка до школи", "program_preschool")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge4to6Message(), keyboard);
    }

    private void showAge6to10Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🏫 Програма початкової школи", "program_primary")
                .addButton("🇬🇧 Англійська мова", "program_english")
                .addRow()
                .addButton("💰 Фінансова грамотність", "program_financial")
                .addButton("🎨 Творчі гуртки", "program_creative")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge6to10Message(), keyboard);
    }

    private void showAge11to15Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Психолог (підлітки)", "program_teen_psychologist")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge11to15Message(), keyboard);
    }

    private void showAge15to18Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Підготовка до НМТ", "program_nmt")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge15to18Message(), keyboard);
    }

    private void showSpecialistsPrograms(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👩‍⚕️ Психолог (4-18 років)", "program_psychologist")
                .addButton("🗣️ Логопед (4-10 років)", "program_speech_therapist")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getSpecialistsMessage(), keyboard);
    }

    private void showProgramDetails(long chatId, String programType) {
        String messageText = switch (programType) {
            case "preschool" -> menuTexts.getProgramPreschoolDetails();
            case "primary" -> menuTexts.getProgramPrimaryDetails();
            case "english" -> menuTexts.getProgramEnglishDetails();
            // TODO Add other cases
            default -> "Деталі програми будуть додані незабаром.";
        };

        var keyboard = new MenuBuilder()
                .addButton("📝 Записатися", "enroll_" + programType)
                .addRow()
                .addButton("⬅️ Назад", getBackButtonForProgram(programType))
                .build();

        messageSender.sendMessage(chatId, messageText, keyboard);
    }

    private String getBackButtonForProgram(String programType) {
        return switch (programType) {
            case "preschool" -> "age_4_6";
            case "primary", "english", "financial", "creative" -> "age_6_10";
            case "teen_psychologist" -> "age_11_15";
            case "nmt" -> "age_15_18";
            case "psychologist", "speech_therapist" -> "age_specialists";
            default -> "programs_main";
        };
    }
}