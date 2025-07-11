package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.service.AdminStateService;
import com.NickSishchuck.StezhkaBot.service.ConsultationService;
import com.NickSishchuck.StezhkaBot.service.EnrollmentService;
import com.NickSishchuck.StezhkaBot.service.StezhkaBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.NickSishchuck.StezhkaBot.service.TextContentService;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

@Component
public class AdminHandler implements MenuHandler {

    private static final Logger logger = LoggerFactory.getLogger(StezhkaBotService.class);
    private final TextContentService textContentService;
    private final AdminStateService adminStateService;
    private final EnrollmentService enrollmentService;
    private final ConsultationService consultationService;
    private TelegramClient telegramClient;
    private MessageSender messageSender;

    // List of admin user IDs
    @Value("${bot.admin.user.ids:}")
    private String adminUserIds;

    @Autowired
    public AdminHandler(TextContentService textContentService, AdminStateService adminStateService,
                        EnrollmentService enrollmentService, ConsultationService consultationService) {
        this.textContentService = textContentService;
        this.adminStateService = adminStateService;
        this.enrollmentService = enrollmentService;
        this.consultationService = consultationService;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("admin_") ||
                callbackData.equals("/admin") ||
                callbackData.startsWith("text_edit_") ||
                callbackData.startsWith("admin_vacation") ||
                callbackData.equals("cancel_edit");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        // Check if user is admin
        if (!isAdmin(chatId)) {
            messageSender.sendMessage(chatId, "❌ Access denied. Admin privileges required.",
                    new MenuBuilder().addButton("⬅️ Back", "main").build());
            return;
        }

        switch (callbackData) {
            case "/admin", "admin_main" -> showAdminMenu(chatId);
            case "admin_vacation_programs" -> showVacationManagement(chatId);
            case "admin_content" -> showContentManagement(chatId);
            case "admin_programs" -> showProgramsManagement(chatId);
            case "admin_age_groups" -> showAgeGroupsManagement(chatId);
            case "admin_age_4_6" -> showAge4to6Management(chatId);
            case "admin_age_6_10" -> showAge6to10Management(chatId);
            case "admin_age_11_15" -> showAge11to15Management(chatId);
            case "admin_age_15_18" -> showAge15to18Management(chatId);
            case "admin_specialists" -> showSpecialistsManagement(chatId);
            case "admin_refresh" -> refreshContent(chatId);
            case "admin_list_all" -> listAllTexts(chatId);
            case "admin_stats" -> showStatistics(chatId);
            case "cancel_edit" -> cancelEditing(chatId);
            default -> {
                if (callbackData.startsWith("text_edit_")) {
                    String key = callbackData.substring("text_edit_".length());
                    startTextEditing(chatId, key);
                }
            }
        }
    }

    private void showVacationManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🍂 Edit Autumn Vacation", "text_edit_PROGRAM_AUTUMN_VACATION_DETAILS")
                .addButton("❄️ Edit Winter Vacation", "text_edit_PROGRAM_WINTER_VACATION_DETAILS")
                .addRow()
                .addButton("🌸 Edit Spring Vacation", "text_edit_PROGRAM_SPRING_VACATION_DETAILS")
                .addButton("☀️ Edit Summer Vacation", "text_edit_PROGRAM_SUMMER_VACATION_DETAILS")
                .addRow()
                .addButton("📝 Edit Vacation Menu", "text_edit_VACATION_MENU_MESSAGE")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🎄 *Vacation Programs Management*\n\nSelect vacation program to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void editVacationManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🍂 Edit Autumn Vacation", "text_edit_PROGRAM_AUTUMN_VACATION_DETAILS")
                .addButton("❄️ Edit Winter Vacation", "text_edit_PROGRAM_WINTER_VACATION_DETAILS")
                .addRow()
                .addButton("🌸 Edit Spring Vacation", "text_edit_PROGRAM_SPRING_VACATION_DETAILS")
                .addButton("☀️ Edit Summer Vacation", "text_edit_PROGRAM_SUMMER_VACATION_DETAILS")
                .addRow()
                .addButton("📝 Edit Vacation Menu", "text_edit_VACATION_MENU_MESSAGE")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🎄 *Vacation Programs Management*\n\nSelect vacation program to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    @Override
    public void handle(long chatId, int messageId, String callbackData) {
        // Check if user is admin
        if (!isAdmin(chatId)) {
            messageSender.editMessage(chatId, messageId, "❌ Access denied. Admin privileges required.",
                    new MenuBuilder().addButton("⬅️ Back", "main").build());
            return;
        }

        switch (callbackData) {
            case "/admin", "admin_main" -> editAdminMenu(chatId, messageId);
            case "admin_content" -> editContentManagement(chatId, messageId);
            case "admin_programs" -> editProgramsManagement(chatId, messageId);
            case "admin_age_groups" -> editAgeGroupsManagement(chatId, messageId);
            case "admin_age_4_6" -> editAge4to6Management(chatId, messageId);
            case "admin_age_6_10" -> editAge6to10Management(chatId, messageId);
            case "admin_age_11_15" -> editAge11to15Management(chatId, messageId);
            case "admin_age_15_18" -> editAge15to18Management(chatId, messageId);
            case "admin_specialists" -> editSpecialistsManagement(chatId, messageId);
            case "admin_refresh" -> refreshContent(chatId, messageId);
            case "admin_list_all" -> listAllTexts(chatId);
            case "admin_stats" -> showStatistics(chatId);
            case "cancel_edit" -> cancelEditing(chatId, messageId);
            default -> {
                if (callbackData.startsWith("text_edit_")) {
                    String key = callbackData.substring("text_edit_".length());
                    startTextEditingWithEdit(chatId, messageId, key);
                }
            }
        }
    }

    /**
     * Process text input from admin (for text updates)
     */
    public boolean processTextInput(long chatId, String messageText) {
        if (!isAdmin(chatId)) {
            return false;
        }

        AdminStateService.EditingState editingState = adminStateService.getEditingState(chatId);
        if (editingState == null) {
            return false;
        }

        String textKey = editingState.getTextKey();

        // Update the text
        boolean success = textContentService.updateText(textKey, messageText);

        if (success) {
            adminStateService.clearEditingState(chatId);

            String successMessage = String.format(
                    "✅ Text updated successfully!\n\n" +
                            "📝 Key: %s\n" +
                            "📏 New length: %d characters",
                    textKey, messageText.length()
            );

            var keyboard = new MenuBuilder()
                    .addButton("📝 Edit Another", getBackButtonForTextKey(textKey))
                    .addButton("⬅️ Back to Admin", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, successMessage, keyboard);
        } else {
            messageSender.sendMessage(chatId, "❌ Failed to update text. Please try again.",
                    new MenuBuilder().addButton("❌ Cancel", "cancel_edit").build());
        }

        return true;
    }

    private void startTextEditing(long chatId, String textKey) {
        String currentText = textContentService.getText(textKey);
        adminStateService.startEditing(chatId, textKey, currentText);

        var keyboard = new MenuBuilder()
                .addButton("❌ Cancel", "cancel_edit")
                .build();

        String message = String.format(
                "📝 *Editing: %s*\n\n" +
                        "Current text:\n" +
                        "═══════════════════\n" +
                        "%s\n" +
                        "═══════════════════\n\n" +
                        "✏️ *Send your new text in the next message*",
                escapeMarkdown(textKey), escapeMarkdown(currentText)
        );

        messageSender.sendMarkdownMessage(chatId, message, keyboard);
    }

    private void startTextEditingWithEdit(long chatId, int messageId, String textKey) {
        String currentText = textContentService.getText(textKey);
        adminStateService.startEditing(chatId, textKey, currentText);

        var keyboard = new MenuBuilder()
                .addButton("❌ Cancel", "cancel_edit")
                .build();

        String message = String.format(
                "📝 *Editing: %s*\n\n" +
                        "Current text:\n" +
                        "═══════════════════\n" +
                        "%s\n" +
                        "═══════════════════\n\n" +
                        "✏️ *Send your new text in the next message*",
                escapeMarkdown(textKey), escapeMarkdown(currentText)
        );

        messageSender.editMarkdownMessage(chatId, messageId, message, keyboard);
    }

    private void cancelEditing(long chatId) {
        AdminStateService.EditingState state = adminStateService.getEditingState(chatId);
        if (state != null) {
            adminStateService.clearEditingState(chatId);

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", getBackButtonForTextKey(state.getTextKey()))
                    .build();

            messageSender.sendMessage(chatId, "❌ Editing cancelled", keyboard);
        } else {
            messageSender.sendMessage(chatId, "Nothing to cancel",
                    new MenuBuilder().addButton("⬅️ Back", "admin_main").build());
        }
    }

    private void cancelEditing(long chatId, int messageId) {
        AdminStateService.EditingState state = adminStateService.getEditingState(chatId);
        if (state != null) {
            adminStateService.clearEditingState(chatId);

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", getBackButtonForTextKey(state.getTextKey()))
                    .build();

            messageSender.editMessage(chatId, messageId, "❌ Editing cancelled", keyboard);
        } else {
            messageSender.editMessage(chatId, messageId, "Nothing to cancel",
                    new MenuBuilder().addButton("⬅️ Back", "admin_main").build());
        }
    }

    private void showAdminMenu(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Content Management", "admin_content")
                .addButton("🎓 Programs Management", "admin_programs")
                .addRow()
                .addButton("📋 Enrollment Requests", "/requests")
                .addButton("📞 Consultations", "/consultations")
                .addRow()
                .addButton("📊 Statistics", "admin_stats")
                .addRow()
                .addButton("🔄 Refresh Cache", "admin_refresh")
                .addButton("📋 List All Texts", "admin_list_all")
                .addRow()
                .addButton("⬅️ Back to Main", "main")
                .build();

        String message = "🔧 Admin Panel\n\nSelect an option:";
        messageSender.sendPlainMessage(chatId, message, keyboard);
    }

    private void editAdminMenu(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Content Management", "admin_content")
                .addButton("🎓 Programs Management", "admin_programs")
                .addRow()
                .addButton("📋 Enrollment Requests", "/requests")
                .addButton("📞 Consultations", "/consultations")
                .addRow()
                .addButton("📊 Statistics", "admin_stats")
                .addRow()
                .addButton("🔄 Refresh Cache", "admin_refresh")
                .addButton("📋 List All Texts", "admin_list_all")
                .addRow()
                .addButton("⬅️ Back to Main", "main")
                .build();

        String message = "🔧 Admin Panel\n\nSelect an option:";
        messageSender.editPlainMessage(chatId, messageId, message, keyboard);
    }

    private void showContentManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Edit Welcome Message", "text_edit_WELCOME_MESSAGE")
                .addButton("📝 Edit Main Menu", "text_edit_MAIN_MENU_MESSAGE")
                .addRow()
                .addButton("📝 Edit Programs Menu", "text_edit_PROGRAMS_MENU_MESSAGE")
                .addButton("📝 Edit FAQ", "text_edit_FAQ_TEXT")
                .addRow()
                .addButton("📝 Edit Contacts", "text_edit_CONTACTS_TEXT")
                .addButton("📝 Edit News", "text_edit_NEWS_TEXT")
                .addRow()
                .addButton("📝 Edit Age Groups", "admin_age_groups")
                .addRow()
                .addButton("⬅️ Back", "admin_main")
                .build();

        String message = "📝 *Content Management*\n\nSelect text to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showProgramsManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Age 4-6 Programs", "admin_age_4_6")
                .addButton("🎒 Age 6-10 Programs", "admin_age_6_10")
                .addRow()
                .addButton("🧠 Age 11-15 Programs", "admin_age_11_15")
                .addButton("🎯 Age 15-18 Programs", "admin_age_15_18")
                .addRow()
                .addButton("🎄 Vacation Programs", "admin_vacation_programs")  // NEW
                .addButton("👨‍⚕️ Specialists Programs", "admin_specialists")
                .addRow()
                .addButton("⬅️ Back", "admin_main")
                .build();

        String message = "🎓 *Programs Management*\n\nSelect category to manage:";
        messageSender.sendMessage(chatId, message, keyboard);
    }


    private void showAge4to6Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Edit Preschool Program", "text_edit_PROGRAM_PRESCHOOL_DETAILS")
                .addRow()
                .addButton("🗣️ Edit Speech Therapist", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addButton("🧠 Edit Neuropsychologist", "text_edit_PROGRAM_NEUROPSYCHOLOGIST_PRESCHOOL_DETAILS")  // NEW
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "👶 *Age 4-6 Programs*\n\nSelect program to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }


    private void showAge6to10Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🏫 Edit Primary School", "text_edit_PROGRAM_PRIMARY_DETAILS")
                .addButton("🇬🇧 Edit English Program", "text_edit_PROGRAM_ENGLISH_DETAILS")
                .addRow()
                .addButton("💰 Edit Financial Literacy", "text_edit_PROGRAM_FINANCIAL_DETAILS")
                .addButton("🎨 Edit Creative Programs", "text_edit_PROGRAM_CREATIVE_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🎒 *Age 6-10 Programs*\n\nSelect program to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showAge11to15Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Edit Teen Psychology", "text_edit_PROGRAM_TEEN_PSYCHOLOGY_DETAILS")
                .addButton("🇬🇧 Edit English (Middle)", "text_edit_PROGRAM_ENGLISH_MIDDLE_DETAILS")  // NEW
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🧠 *Age 11-15 Programs*\n\nSelect program to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }


    private void showAge15to18Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Edit NMT Preparation", "text_edit_PROGRAM_NMT_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🎯 *Age 15-18 Programs*\n\nSelect program to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showSpecialistsManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👩‍⚕️ Edit Psychologist", "text_edit_PROGRAM_PSYCHOLOGIST_DETAILS")
                .addButton("🗣️ Edit Speech Therapist", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addRow()
                .addButton("🧠 Edit Neuropedagog", "text_edit_PROGRAM_NEUROPEDAGOG_DETAILS")  // NEW
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "👨‍⚕️ *Specialists Programs*\n\nSelect program to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }


    private void showAgeGroupsManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Edit Age 4-6 Info", "text_edit_AGE_4_6_MESSAGE")
                .addButton("🎒 Edit Age 6-10 Info", "text_edit_AGE_6_10_MESSAGE")
                .addRow()
                .addButton("🧠 Edit Age 11-15 Info", "text_edit_AGE_11_15_MESSAGE")
                .addButton("🎯 Edit Age 15-18 Info", "text_edit_AGE_15_18_MESSAGE")
                .addRow()
                .addButton("👨‍⚕️ Edit Specialists Info", "text_edit_SPECIALISTS_MESSAGE")
                .addRow()
                .addButton("⬅️ Back", "admin_content")
                .build();

        String message = "📝 *Age Groups Information*\n\nSelect age group info to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    /**
     * Escape Markdown special characters
     */
    private String escapeMarkdown(String text) {
        if (text == null) return "";

        // First, escape backslashes
        text = text.replace("\\", "\\\\");

        // Then escape other special characters
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    // Edit methods for all admin panels
    private void editContentManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Edit Welcome Message", "text_edit_WELCOME_MESSAGE")
                .addButton("📝 Edit Main Menu", "text_edit_MAIN_MENU_MESSAGE")
                .addRow()
                .addButton("📝 Edit Programs Menu", "text_edit_PROGRAMS_MENU_MESSAGE")
                .addButton("📝 Edit FAQ", "text_edit_FAQ_TEXT")
                .addRow()
                .addButton("📝 Edit Contacts", "text_edit_CONTACTS_TEXT")
                .addButton("📝 Edit News", "text_edit_NEWS_TEXT")
                .addRow()
                .addButton("📝 Edit Age Groups", "admin_age_groups")
                .addRow()
                .addButton("⬅️ Back", "admin_main")
                .build();

        String message = "📝 *Content Management*\n\nSelect text to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editProgramsManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Age 4-6 Programs", "admin_age_4_6")
                .addButton("🎒 Age 6-10 Programs", "admin_age_6_10")
                .addRow()
                .addButton("🧠 Age 11-15 Programs", "admin_age_11_15")
                .addButton("🎯 Age 15-18 Programs", "admin_age_15_18")
                .addRow()
                .addButton("👨‍⚕️ Specialists Programs", "admin_specialists")
                .addRow()
                .addButton("⬅️ Back", "admin_main")
                .build();

        String message = "🎓 *Programs Management*\n\nSelect age group to manage:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge4to6Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Edit Preschool Program", "text_edit_PROGRAM_PRESCHOOL_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "👶 *Age 4-6 Programs*\n\nSelect program to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge6to10Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🏫 Edit Primary School", "text_edit_PROGRAM_PRIMARY_DETAILS")
                .addButton("🇬🇧 Edit English Program", "text_edit_PROGRAM_ENGLISH_DETAILS")
                .addRow()
                .addButton("💰 Edit Financial Literacy", "text_edit_PROGRAM_FINANCIAL_DETAILS")
                .addButton("🎨 Edit Creative Programs", "text_edit_PROGRAM_CREATIVE_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🎒 *Age 6-10 Programs*\n\nSelect program to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge11to15Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Edit Teen Psychology", "text_edit_PROGRAM_TEEN_PSYCHOLOGY_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🧠 *Age 11-15 Programs*\n\nSelect program to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge15to18Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Edit NMT Preparation", "text_edit_PROGRAM_NMT_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "🎯 *Age 15-18 Programs*\n\nSelect program to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editSpecialistsManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👩‍⚕️ Edit Psychologist", "text_edit_PROGRAM_PSYCHOLOGIST_DETAILS")
                .addButton("🗣️ Edit Speech Therapist", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addRow()
                .addButton("⬅️ Back", "admin_programs")
                .build();

        String message = "👨‍⚕️ *Specialists Programs*\n\nSelect program to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAgeGroupsManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Edit Age 4-6 Info", "text_edit_AGE_4_6_MESSAGE")
                .addButton("🎒 Edit Age 6-10 Info", "text_edit_AGE_6_10_MESSAGE")
                .addRow()
                .addButton("🧠 Edit Age 11-15 Info", "text_edit_AGE_11_15_MESSAGE")
                .addButton("🎯 Edit Age 15-18 Info", "text_edit_AGE_15_18_MESSAGE")
                .addRow()
                .addButton("👨‍⚕️ Edit Specialists Info", "text_edit_SPECIALISTS_MESSAGE")
                .addRow()
                .addButton("⬅️ Back", "admin_content")
                .build();

        String message = "📝 *Age Groups Information*\n\nSelect age group info to edit:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void refreshContent(long chatId, int messageId) {
        try {
            textContentService.refreshCache();

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", "admin_main")
                    .build();

            messageSender.editMessage(chatId, messageId, "✅ Content cache refreshed successfully!", keyboard);
        } catch (Exception e) {
            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", "admin_main")
                    .build();

            messageSender.editMessage(chatId, messageId, "❌ Failed to refresh cache: " + e.getMessage(), keyboard);
        }
    }

    private void refreshContent(long chatId) {
        try {
            textContentService.refreshCache();

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, "✅ Content cache refreshed successfully!", keyboard);
        } catch (Exception e) {
            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, "❌ Failed to refresh cache: " + e.getMessage(), keyboard);
        }
    }

    private void listAllTexts(long chatId) {
        Map<String, String> allTexts = textContentService.getAllTexts();

        StringBuilder message = new StringBuilder("📋 All Text Entries:\n\n");

        allTexts.entrySet().stream()
                .limit(10)
                .forEach(entry -> {
                    String preview = entry.getValue().length() > 50
                            ? entry.getValue().substring(0, 50) + "..."
                            : entry.getValue();
                    message.append(String.format("• %s: %s\n", entry.getKey(), preview));
                });

        if (allTexts.size() > 10) {
            message.append(String.format("\n... and %d more entries", allTexts.size() - 10));
        }

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Back", "admin_main")
                .build();

        messageSender.sendPlainMessage(chatId, message.toString(), keyboard);
    }

    private void showStatistics(long chatId) {
        String enrollmentStats = enrollmentService.getStatistics();
        long unprocessedConsultations = consultationService.getUnprocessedConsultationsCount();

        String stats = enrollmentStats + "\n\n" +
                "📞 *Консультації*\n" +
                "⏳ Необроблені: " + unprocessedConsultations;

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Back", "admin_main")
                .build();
        messageSender.sendMessage(chatId, stats, keyboard);
    }

    private String getBackButtonForTextKey(String textKey) {
        // Return appropriate back button based on text key
        if (textKey.startsWith("PROGRAM_")) {
            if (textKey.contains("PRESCHOOL") || textKey.contains("NEUROPSYCHOLOGIST_PRESCHOOL")) return "admin_age_4_6";
            if (textKey.contains("PRIMARY") || textKey.contains("ENGLISH") ||
                    textKey.contains("FINANCIAL") || textKey.contains("CREATIVE")) return "admin_age_6_10";
            if (textKey.contains("TEEN") || textKey.contains("ENGLISH_MIDDLE")) return "admin_age_11_15";
            if (textKey.contains("NMT")) return "admin_age_15_18";
            if (textKey.contains("PSYCHOLOGIST") || textKey.contains("SPEECH") || textKey.contains("NEUROPEDAGOG")) return "admin_specialists";
            if (textKey.contains("VACATION") || textKey.contains("AUTUMN") || textKey.contains("WINTER") ||
                    textKey.contains("SPRING") || textKey.contains("SUMMER")) return "admin_vacation_programs";
            return "admin_programs";
        }
        if (textKey.startsWith("AGE_") || textKey.equals("SPECIALISTS_MESSAGE")) {
            return "admin_age_groups";
        }
        if (textKey.equals("VACATION_MENU_MESSAGE")) {
            return "admin_vacation_programs";
        }
        return "admin_content";
    }

    // Helper method to check if user is admin
    private boolean isAdmin(long userId) {
        if (adminUserIds == null || adminUserIds.isEmpty()) {
            return false; // No admins configured
        }

        String[] adminIds = adminUserIds.split(",");
        String userIdStr = String.valueOf(userId);

        for (String adminId : adminIds) {
            if (adminId.trim().equals(userIdStr)) {
                return true;
            }
        }
        return false;
    }
}