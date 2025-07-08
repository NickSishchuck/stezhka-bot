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

            // Age 4-6 programs
            case "program_preschool" -> showProgramDetails(chatId, "preschool");

            // Age 6-10 programs
            case "program_primary" -> showProgramDetails(chatId, "primary");
            case "program_english" -> showProgramDetails(chatId, "english");
            case "program_financial" -> showProgramDetails(chatId, "financial");
            case "program_creative" -> showProgramDetails(chatId, "creative");

            // Age 11-15 programs
            case "program_teen_psychologist" -> showProgramDetails(chatId, "teen_psychology");

            // Age 15-18 programs
            case "program_nmt" -> showProgramDetails(chatId, "nmt");

            // Specialists programs
            case "program_psychologist" -> showProgramDetails(chatId, "psychologist");
            case "program_speech_therapist" -> showProgramDetails(chatId, "speech_therapist");
        }
    }

    @Override
    public void handle(long chatId, int messageId, String callbackData) {
        switch (callbackData) {
            case "programs_main" -> editProgramsMenu(chatId, messageId);
            case "age_4_6" -> editAge4to6Programs(chatId, messageId);
            case "age_6_10" -> editAge6to10Programs(chatId, messageId);
            case "age_11_15" -> editAge11to15Programs(chatId, messageId);
            case "age_15_18" -> editAge15to18Programs(chatId, messageId);
            case "age_specialists" -> editSpecialistsPrograms(chatId, messageId);

            // Age 4-6 programs
            case "program_preschool" -> editProgramDetails(chatId, messageId, "preschool");

            // Age 6-10 programs
            case "program_primary" -> editProgramDetails(chatId, messageId, "primary");
            case "program_english" -> editProgramDetails(chatId, messageId, "english");
            case "program_financial" -> editProgramDetails(chatId, messageId, "financial");
            case "program_creative" -> editProgramDetails(chatId, messageId, "creative");

            // Age 11-15 programs
            case "program_teen_psychologist" -> editProgramDetails(chatId, messageId, "teen_psychology");

            // Age 15-18 programs
            case "program_nmt" -> editProgramDetails(chatId, messageId, "nmt");

            // Specialists programs
            case "program_psychologist" -> editProgramDetails(chatId, messageId, "psychologist");
            case "program_speech_therapist" -> editProgramDetails(chatId, messageId, "speech_therapist");
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

    private void editProgramsMenu(long chatId, int messageId) {
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

        messageSender.editMessage(chatId, messageId, menuTexts.getProgramsMenuMessage(), keyboard);
    }

    private void showAge4to6Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Підготовка до школи", "program_preschool")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge4to6Message(), keyboard);
    }

    private void editAge4to6Programs(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Підготовка до школи", "program_preschool")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getAge4to6Message(), keyboard);
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

    private void editAge6to10Programs(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🏫 Програма початкової школи", "program_primary")
                .addButton("🇬🇧 Англійська мова", "program_english")
                .addRow()
                .addButton("💰 Фінансова грамотність", "program_financial")
                .addButton("🎨 Творчі гуртки", "program_creative")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getAge6to10Message(), keyboard);
    }

    private void showAge11to15Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Психолог (підлітки)", "program_teen_psychologist")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge11to15Message(), keyboard);
    }

    private void editAge11to15Programs(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Психолог (підлітки)", "program_teen_psychologist")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getAge11to15Message(), keyboard);
    }

    private void showAge15to18Programs(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Підготовка до НМТ", "program_nmt")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.sendMessage(chatId, menuTexts.getAge15to18Message(), keyboard);
    }

    private void editAge15to18Programs(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Підготовка до НМТ", "program_nmt")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getAge15to18Message(), keyboard);
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

    private void editSpecialistsPrograms(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👩‍⚕️ Психолог (4-18 років)", "program_psychologist")
                .addButton("🗣️ Логопед (4-10 років)", "program_speech_therapist")
                .addRow()
                .addButton("⬅️ Назад", "programs_main")
                .build();

        messageSender.editMessage(chatId, messageId, menuTexts.getSpecialistsMessage(), keyboard);
    }

    private void showProgramDetails(long chatId, String programType) {
        String messageText = switch (programType) {
            case "preschool" -> menuTexts.getProgramPreschoolDetails();
            case "primary" -> menuTexts.getProgramPrimaryDetails();
            case "english" -> menuTexts.getProgramEnglishDetails();
            case "financial" -> menuTexts.getProgramFinancialDetails();
            case "creative" -> menuTexts.getProgramCreativeDetails();
            case "teen_psychology" -> menuTexts.getProgramTeenPsychologyDetails();
            case "nmt" -> menuTexts.getProgramNmtDetails();
            case "psychologist" -> menuTexts.getProgramPsychologistDetails();
            case "speech_therapist" -> menuTexts.getProgramSpeechTherapistDetails();
            default -> "Деталі програми будуть додані незабаром.";
        };

        var keyboard = new MenuBuilder()
                .addButton("📝 Записатися", "enroll_" + programType)
                .addRow()
                .addButton("⬅️ Назад", getBackButtonForProgram(programType))
                .build();

        messageSender.sendMessage(chatId, messageText, keyboard);
    }

    private void editProgramDetails(long chatId, int messageId, String programType) {
        String messageText = switch (programType) {
            case "preschool" -> menuTexts.getProgramPreschoolDetails();
            case "primary" -> menuTexts.getProgramPrimaryDetails();
            case "english" -> menuTexts.getProgramEnglishDetails();
            case "financial" -> menuTexts.getProgramFinancialDetails();
            case "creative" -> menuTexts.getProgramCreativeDetails();
            case "teen_psychology" -> menuTexts.getProgramTeenPsychologyDetails();
            case "nmt" -> menuTexts.getProgramNmtDetails();
            case "psychologist" -> menuTexts.getProgramPsychologistDetails();
            case "speech_therapist" -> menuTexts.getProgramSpeechTherapistDetails();
            default -> "Деталі програми будуть додані незабаром.";
        };

        var keyboard = new MenuBuilder()
                .addButton("📝 Записатися", "enroll_" + programType)
                .addRow()
                .addButton("⬅️ Назад", getBackButtonForProgram(programType))
                .build();

        messageSender.editMessage(chatId, messageId, messageText, keyboard);
    }

    private String getBackButtonForProgram(String programType) {
        return switch (programType) {
            case "preschool" -> "age_4_6";
            case "primary", "english", "financial", "creative" -> "age_6_10";
            case "teen_psychology" -> "age_11_15";
            case "nmt" -> "age_15_18";
            case "psychologist", "speech_therapist" -> "age_specialists";
            default -> "programs_main";
        };
    }
}