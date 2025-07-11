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
            messageSender.sendMessage(chatId, "❌ Доступ заблоковано",
                    new MenuBuilder().addButton("⬅️ Назад", "main").build());
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
                .addButton("🍂 Осінні канікули", "text_edit_PROGRAM_AUTUMN_VACATION_DETAILS")
                .addButton("❄️ Зимові канікули", "text_edit_PROGRAM_WINTER_VACATION_DETAILS")
                .addRow()
                .addButton("🌸 Весінні канікули", "text_edit_PROGRAM_SPRING_VACATION_DETAILS")
                .addButton("☀️ Літні канікули", "text_edit_PROGRAM_SUMMER_VACATION_DETAILS")
                .addRow()
                .addButton("📝 Меню канікул", "text_edit_VACATION_MENU_MESSAGE")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🎄 *Менеджмент канікулярних програм*\n\nОберіть текст для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void editVacationManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🍂 Осінні канікули", "text_edit_PROGRAM_AUTUMN_VACATION_DETAILS")
                .addButton("❄️ Зимові канікули", "text_edit_PROGRAM_WINTER_VACATION_DETAILS")
                .addRow()
                .addButton("🌸 Весінні канікули", "text_edit_PROGRAM_SPRING_VACATION_DETAILS")
                .addButton("☀️ Літні канікули", "text_edit_PROGRAM_SUMMER_VACATION_DETAILS")
                .addRow()
                .addButton("📝 Меню канікул", "text_edit_VACATION_MENU_MESSAGE")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🎄 *Менеджмент канікулярних програм*\n\nОберіть канікулярну програму для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    @Override
    public void handle(long chatId, int messageId, String callbackData) {
        // Check if user is admin
        if (!isAdmin(chatId)) {
            messageSender.editMessage(chatId, messageId, "❌ В доступі відхилено.",
                    new MenuBuilder().addButton("⬅️ Назад", "main").build());
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
            case "admin_stats" -> editStatistics(chatId, messageId);
            case "admin_vacation_programs" -> editVacationManagement(chatId, messageId);
            case "cancel_edit" -> cancelEditing(chatId, messageId);
            case "/requests" -> {
                // Redirect to enrollment requests - edit current message to show we're redirecting
                messageSender.editMessage(chatId, messageId,
                        "📋 Завантаження заявок на зарахування...",
                        new MenuBuilder().build());
                // Then show the requests (this will send a new message)
                showEnrollmentRequests(chatId);
            }
            case "/consultations" -> {
                // Redirect to consultations - edit current message to show we're redirecting
                messageSender.editMessage(chatId, messageId,
                        "📞 Завантаження консультацій...",
                        new MenuBuilder().build());
                // Then show the consultations (this will send a new message)
                showConsultations(chatId);
            }
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
                    "✅ Текст редаговано успішно!\n\n" +
                            "📝 Ключ: %s\n" +
                            "📏 Нова довжина тексту: %d символів",
                    textKey, messageText.length()
            );

            var keyboard = new MenuBuilder()
                    .addButton("📝 Редагувати ще", getBackButtonForTextKey(textKey))
                    .addButton("⬅️ Назад в АдмінМеню", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, successMessage, keyboard);
        } else {
            messageSender.sendMessage(chatId, "❌ Щось пішло не так. Спробуйте ще раз.",
                    new MenuBuilder().addButton("❌ Відмінити", "cancel_edit").build());
        }

        return true;
    }

    private void startTextEditing(long chatId, String textKey) {
        String currentText = textContentService.getText(textKey);
        adminStateService.startEditing(chatId, textKey, currentText);

        var keyboard = new MenuBuilder()
                .addButton("❌ Відмінити", "cancel_edit")
                .build();

        String message = String.format(
                "📝 *Редагую: %s*\n\n" +
                        "Текст зараз:\n" +
                        "═══════════════════\n" +
                        "%s\n" +
                        "═══════════════════\n\n" +
                        "✏️ Відправте нове повідомлення щоб замінити старе",
                escapeMarkdown(textKey), escapeMarkdown(currentText)
        );

        messageSender.sendMarkdownMessage(chatId, message, keyboard);
    }

    private void startTextEditingWithEdit(long chatId, int messageId, String textKey) {
        String currentText = textContentService.getText(textKey);
        adminStateService.startEditing(chatId, textKey, currentText);

        var keyboard = new MenuBuilder()
                .addButton("❌ Відмінити", "cancel_edit")
                .build();

        String message = String.format(
                "📝 *Редагую: %s*\n\n" +
                        "Текст зараз:\n" +
                        "═══════════════════\n" +
                        "%s\n" +
                        "═══════════════════\n\n" +
                        "✏️ Відправте нове повідомлення щоб замінити старе",
                escapeMarkdown(textKey), escapeMarkdown(currentText)
        );

        messageSender.editMarkdownMessage(chatId, messageId, message, keyboard);
    }

    private void cancelEditing(long chatId) {
        AdminStateService.EditingState state = adminStateService.getEditingState(chatId);
        if (state != null) {
            adminStateService.clearEditingState(chatId);

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Назад", getBackButtonForTextKey(state.getTextKey()))
                    .build();

            messageSender.sendMessage(chatId, "❌ Редагування відмінено", keyboard);
        } else {
            messageSender.sendMessage(chatId, "Нічого відміняти",
                    new MenuBuilder().addButton("⬅️ Назад", "admin_main").build());
        }
    }

    private void cancelEditing(long chatId, int messageId) {
        AdminStateService.EditingState state = adminStateService.getEditingState(chatId);
        if (state != null) {
            adminStateService.clearEditingState(chatId);

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Назад", getBackButtonForTextKey(state.getTextKey()))
                    .build();

            messageSender.editMessage(chatId, messageId, "❌ Редагування відмінено", keyboard);
        } else {
            messageSender.editMessage(chatId, messageId, "Нічого відмінювати",
                    new MenuBuilder().addButton("⬅️ Назад", "admin_main").build());
        }
    }

    private void showAdminMenu(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Контент Менеджмент", "admin_content")
                .addButton("🎓 Програми", "admin_programs")
                .addRow()
                .addButton("📊 Статистика", "admin_stats")
                .addButton("🔄 Оновлення кешу", "admin_refresh")
                .addRow()
                .addButton("⬅️ Назад на Головну", "main")
                .build();

        String message = "🔧 Панель адміністратора\n\n/requests; /consultations";
        messageSender.sendPlainMessage(chatId, message, keyboard);
    }

    private void editAdminMenu(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Контент менеджмент", "admin_content")
                .addButton("🎓 Програми", "admin_programs")
                .addRow()
                .addButton("📊 Статистика", "admin_stats")
                .addButton("🔄 Оновлення кешу", "admin_refresh")
                .addRow()
                .addButton("⬅️ Назад на Головну", "main")
                .build();

        String message = "🔧 Панель адміністратора\n\nОберіть опцію:";
        messageSender.editPlainMessage(chatId, messageId, message, keyboard);
    }

    private void showContentManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Редагувати вітальне повідомлення", "text_edit_WELCOME_MESSAGE")
                .addButton("📝 Редагувати головне меню", "text_edit_MAIN_MENU_MESSAGE")
                .addRow()
                .addButton("📝 Редагувати меню програм", "text_edit_PROGRAMS_MENU_MESSAGE")
                .addButton("📝 Редагувати FAQ", "text_edit_FAQ_TEXT")
                .addRow()
                .addButton("📝 Редагувати контакти", "text_edit_CONTACTS_TEXT")
                .addButton("📝 Редагувати новини", "text_edit_NEWS_TEXT")
                .addRow()
                .addButton("📝 Редагувати вікові групи", "admin_age_groups")
                .addRow()
                .addButton("⬅️ Назад", "admin_main")
                .build();

        String message = "📝 *Менеджмент контенту*\n\nОберіть текст для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showProgramsManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Програми 4-6 років", "admin_age_4_6")
                .addButton("🎒 Програми 6-10 років", "admin_age_6_10")
                .addRow()
                .addButton("🧠 Програми 11-15 років", "admin_age_11_15")
                .addButton("🎯 Програми 15-18 років", "admin_age_15_18")
                .addRow()
                .addButton("🎄 Канікулярні програми", "admin_vacation_programs")
                .addButton("👨‍⚕️ Програми спеціалістів", "admin_specialists")
                .addRow()
                .addButton("⬅️ Назад", "admin_main")
                .build();

        String message = "🎓 *Менеджмент програм*\n\nОберіть категорію для управління:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showAge4to6Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Редагувати дошкільну програму", "text_edit_PROGRAM_PRESCHOOL_DETAILS")
                .addRow()
                .addButton("🗣️ Редагувати логопеда", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addButton("🧠 Редагувати нейропсихолога", "text_edit_PROGRAM_NEUROPSYCHOLOGIST_PRESCHOOL_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "👶 *Програми 4-6 років*\n\nОберіть програму для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showAge6to10Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🏫 Редагувати початкову школу", "text_edit_PROGRAM_PRIMARY_DETAILS")
                .addButton("🇬🇧 Редагувати англійську програму", "text_edit_PROGRAM_ENGLISH_DETAILS")
                .addRow()
                .addButton("💰 Редагувати фінансову грамотність", "text_edit_PROGRAM_FINANCIAL_DETAILS")
                .addButton("🎨 Редагувати творчі програми", "text_edit_PROGRAM_CREATIVE_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🎒 *Програми 6-10 років*\n\nОберіть програму для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showAge11to15Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Редагувати підліткову психологію", "text_edit_PROGRAM_TEEN_PSYCHOLOGY_DETAILS")
                .addButton("🇬🇧 Редагувати англійську (середня)", "text_edit_PROGRAM_ENGLISH_MIDDLE_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🧠 *Програми 11-15 років*\n\nОберіть програму для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showAge15to18Management(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Редагувати підготовку до НМТ", "text_edit_PROGRAM_NMT_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🎯 *Програми 15-18 років*\n\nОберіть програму для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showSpecialistsManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👩‍⚕️ Редагувати психолога", "text_edit_PROGRAM_PSYCHOLOGIST_DETAILS")
                .addButton("🗣️ Редагувати логопеда", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addRow()
                .addButton("🧠 Редагувати нейропедагога", "text_edit_PROGRAM_NEUROPEDAGOG_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "👨‍⚕️ *Програми спеціалістів*\n\nОберіть програму для редагування:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showAgeGroupsManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Редагувати інфо 4-6 років", "text_edit_AGE_4_6_MESSAGE")
                .addButton("🎒 Редагувати інфо 6-10 років", "text_edit_AGE_6_10_MESSAGE")
                .addRow()
                .addButton("🧠 Редагувати інфо 11-15 років", "text_edit_AGE_11_15_MESSAGE")
                .addButton("🎯 Редагувати інфо 15-18 років", "text_edit_AGE_15_18_MESSAGE")
                .addRow()
                .addButton("👨‍⚕️ Редагувати інфо спеціалістів", "text_edit_SPECIALISTS_MESSAGE")
                .addRow()
                .addButton("⬅️ Назад", "admin_content")
                .build();

        String message = "📝 *Інформація про вікові групи*\n\nОберіть інформацію вікової групи для редагування:";
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
                .addButton("📝 Редагувати вітальне повідомлення", "text_edit_WELCOME_MESSAGE")
                .addButton("📝 Редагувати головне меню", "text_edit_MAIN_MENU_MESSAGE")
                .addRow()
                .addButton("📝 Редагувати меню програм", "text_edit_PROGRAMS_MENU_MESSAGE")
                .addButton("📝 Редагувати FAQ", "text_edit_FAQ_TEXT")
                .addRow()
                .addButton("📝 Редагувати контакти", "text_edit_CONTACTS_TEXT")
                .addButton("📝 Редагувати новини", "text_edit_NEWS_TEXT")
                .addRow()
                .addButton("📝 Редагувати вікові групи", "admin_age_groups")
                .addRow()
                .addButton("⬅️ Назад", "admin_main")
                .build();

        String message = "📝 *Менеджмент контенту*\n\nОберіть текст для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editProgramsManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Програми 4-6 років", "admin_age_4_6")
                .addButton("🎒 Програми 6-10 років", "admin_age_6_10")
                .addRow()
                .addButton("🧠 Програми 11-15 років", "admin_age_11_15")
                .addButton("🎯 Програми 15-18 років", "admin_age_15_18")
                .addRow()
                .addButton("🎄 Канікулярні програми", "admin_vacation_programs")
                .addButton("👨‍⚕️ Програми спеціалістів", "admin_specialists")
                .addRow()
                .addButton("⬅️ Назад", "admin_main")
                .build();

        String message = "🎓 *Менеджмент програм*\n\nОберіть категорію для управління:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge4to6Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("📚 Редагувати дошкільну програму", "text_edit_PROGRAM_PRESCHOOL_DETAILS")
                .addRow()
                .addButton("🗣️ Редагувати логопеда", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addButton("🧠 Редагувати нейропсихолога", "text_edit_PROGRAM_NEUROPSYCHOLOGIST_PRESCHOOL_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "👶 *Програми 4-6 років*\n\nОберіть програму для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge6to10Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🏫 Редагувати початкову школу", "text_edit_PROGRAM_PRIMARY_DETAILS")
                .addButton("🇬🇧 Редагувати англійську програму", "text_edit_PROGRAM_ENGLISH_DETAILS")
                .addRow()
                .addButton("💰 Редагувати фінансову грамотність", "text_edit_PROGRAM_FINANCIAL_DETAILS")
                .addButton("🎨 Редагувати творчі програми", "text_edit_PROGRAM_CREATIVE_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🎒 *Програми 6-10 років*\n\nОберіть програму для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge11to15Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🧠 Редагувати підліткову психологію", "text_edit_PROGRAM_TEEN_PSYCHOLOGY_DETAILS")
                .addButton("🇬🇧 Редагувати англійську (середня)", "text_edit_PROGRAM_ENGLISH_MIDDLE_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🧠 *Програми 11-15 років*\n\nОберіть програму для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAge15to18Management(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("🎯 Редагувати підготовку до НМТ", "text_edit_PROGRAM_NMT_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "🎯 *Програми 15-18 років*\n\nОберіть програму для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editSpecialistsManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👩‍⚕️ Редагувати психолога", "text_edit_PROGRAM_PSYCHOLOGIST_DETAILS")
                .addButton("🗣️ Редагувати логопеда", "text_edit_PROGRAM_SPEECH_THERAPIST_DETAILS")
                .addRow()
                .addButton("🧠 Редагувати нейропедагога", "text_edit_PROGRAM_NEUROPEDAGOG_DETAILS")
                .addRow()
                .addButton("⬅️ Назад", "admin_programs")
                .build();

        String message = "👨‍⚕️ *Програми спеціалістів*\n\nОберіть програму для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void editAgeGroupsManagement(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                .addButton("👶 Редагувати інфо 4-6 років", "text_edit_AGE_4_6_MESSAGE")
                .addButton("🎒 Редагувати інфо 6-10 років", "text_edit_AGE_6_10_MESSAGE")
                .addRow()
                .addButton("🧠 Редагувати інфо 11-15 років", "text_edit_AGE_11_15_MESSAGE")
                .addButton("🎯 Редагувати інфо 15-18 років", "text_edit_AGE_15_18_MESSAGE")
                .addRow()
                .addButton("👨‍⚕️ Редагувати інфо спеціалістів", "text_edit_SPECIALISTS_MESSAGE")
                .addRow()
                .addButton("⬅️ Назад", "admin_content")
                .build();

        String message = "📝 *Інформація про вікові групи*\n\nОберіть інформацію вікової групи для редагування:";
        messageSender.editMessage(chatId, messageId, message, keyboard);
    }

    private void refreshContent(long chatId, int messageId) {
        try {
            textContentService.refreshCache();

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Назад", "admin_main")
                    .build();

            messageSender.editMessage(chatId, messageId, "✅ Кеш контенту успішно оновлено!", keyboard);
        } catch (Exception e) {
            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Назад", "admin_main")
                    .build();

            messageSender.editMessage(chatId, messageId, "❌ Не вдалося оновити кеш: " + e.getMessage(), keyboard);
        }
    }

    private void refreshContent(long chatId) {
        try {
            textContentService.refreshCache();

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Назад", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, "✅ Кеш контенту успішно оновлено!", keyboard);
        } catch (Exception e) {
            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Назад", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, "❌ Не вдалося оновити кеш: " + e.getMessage(), keyboard);
        }
    }

    private void showStatistics(long chatId) {
        String enrollmentStats = enrollmentService.getStatistics();
        long unprocessedConsultations = consultationService.getUnprocessedConsultationsCount();

        String stats = enrollmentStats + "\n\n" +
                "📞 *Консультації*\n" +
                "⏳ Необроблені: " + unprocessedConsultations;

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "admin_main")
                .build();
        messageSender.sendMessage(chatId, stats, keyboard);
    }

    private void editStatistics(long chatId, int messageId) {
        String enrollmentStats = enrollmentService.getStatistics();
        long unprocessedConsultations = consultationService.getUnprocessedConsultationsCount();

        String stats = enrollmentStats + "\n\n" +
                "📞 *Консультації*\n" +
                "⏳ Необроблені: " + unprocessedConsultations;

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Назад", "admin_main")
                .build();
        messageSender.editMessage(chatId, messageId, stats, keyboard);
    }

    // Helper methods for redirecting to other handlers
    private void showEnrollmentRequests(long chatId) {
        try {

            messageSender.sendMessage(chatId, "📋 Використовуйте команду /requests для перегляду заявок на зарахування.",
                    new MenuBuilder().addButton("⬅️ Назад до Адмін", "admin_main").build());
        } catch (Exception e) {
            logger.error("Failed to show enrollment requests", e);
        }
    }

    private void showConsultations(long chatId) {
        try {
            messageSender.sendMessage(chatId, "📞 Використовуйте команду /consultations для перегляду консультацій.",
                    new MenuBuilder().addButton("⬅️ Назад до Адмін", "admin_main").build());
        } catch (Exception e) {
            logger.error("Failed to show consultations", e);
        }
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