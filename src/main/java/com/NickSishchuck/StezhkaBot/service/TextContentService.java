package com.NickSishchuck.StezhkaBot.service;

import com.NickSishchuck.StezhkaBot.entity.TextContent;
import com.NickSishchuck.StezhkaBot.repository.TextContentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Service
public class TextContentService {

    private static final Logger logger = LoggerFactory.getLogger(TextContentService.class);

    private final TextContentRepository textContentRepository;
    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    @Autowired
    private AdminNotificationService notificationService;

    @Autowired
    public TextContentService(TextContentRepository textContentRepository) {
        this.textContentRepository = textContentRepository;
    }

    /**
     * Load all text content from database on startup
     */
    @PostConstruct
    public void loadAllTexts() {
        try {
            logger.info("Loading text content from database...");

            List<TextContent> allTexts = textContentRepository.findAll();

            if (allTexts.isEmpty()) {
                logger.warn("No text content found in database. Initializing with default values...");
                initializeDefaultTexts();
                return;
            }

            // Load into cache
            for (TextContent content : allTexts) {
                textCache.put(content.getTextKey(), content.getTextValue());
            }

            logger.info("Successfully loaded {} text entries from database", textCache.size());

        } catch (Exception e) {
            logger.error("Failed to load text content from database. Using fallback values.", e);
            initializeFallbackTexts();
        }
    }

    /**
     * Get text by key name
     */
    public String getText(String key) {
        String text = textCache.get(key);
        if (text == null) {
            logger.warn("Text key '{}' not found in cache. Returning key as fallback.", key);
            return key; // Return key as fallback
        }
        return text;
    }

    /**
     * Update text content and save to database
     */
    @Transactional
    public boolean updateText(String key, String newValue) {
        try {
            TextContent content = textContentRepository.findByTextKey(key)
                    .orElse(new TextContent(key, newValue, "Updated via bot"));

            content.setTextValue(newValue);
            textContentRepository.save(content);

            // Update cache
            textCache.put(key, newValue);

            logger.info("Successfully updated text key '{}' with new value", key);
            return true;

        } catch (Exception e) {
            logger.error("Failed to update text key '{}'", key, e);
            return false;
        }
    }

    /**
     * Create new text entry
     */
    @Transactional
    public boolean createText(String key, String value, String description) {
        try {
            if (textContentRepository.existsByTextKey(key)) {
                logger.warn("Text key '{}' already exists", key);
                return false;
            }

            TextContent content = new TextContent(key, value, description);
            textContentRepository.save(content);

            // Update cache
            textCache.put(key, value);

            logger.info("Successfully created new text key '{}'", key);
            return true;

        } catch (Exception e) {
            logger.error("Failed to create text key '{}'", key, e);
            return false;
        }
    }

    /**
     * Refresh cache from database
     */
    public void refreshCache() {
        logger.info("Refreshing text content cache...");
        textCache.clear();
        loadAllTexts();
    }

    /**
     * Get all cached texts (for debugging/admin purposes)
     */
    public Map<String, String> getAllTexts() {
        return Map.copyOf(textCache);
    }

    /**
     * Initialize database with default text values
     */
    @Transactional
    public void initializeDefaultTexts() {
        logger.info("Initializing database with default text values...");

        // Main menu texts
        createTextIfNotExists("WELCOME_MESSAGE",
                "🌟 *Вітаємо в Центрі діяльнісної освіти «Стежка»\\!*\n\n" +
                        "Ми допомагаємо дітям віком 4\\-18 років розвиватися через цікаві та корисні програми\\.\n\n" +
                        "Оберіть дію нижче👇",
                "Welcome message shown when user starts the bot");

        createTextIfNotExists("MAIN_MENU_MESSAGE",
                "🏠 *Головне меню*\n\n" +
                        "Оберіть розділ:",
                "Main menu message");

        createTextIfNotExists("PROGRAMS_MENU_MESSAGE",
                "🎓 *Навчальні програми*\n\n" +
                        "Оберіть вікову категорію:",
                "Programs menu message");

        // Age group descriptions
        createTextIfNotExists("AGE_4_6_MESSAGE",
                "👶 *Програми для дошкільнят (4-6 років)*\n\n" +
                        "Розвиваючі програми для підготовки до школи та загального розвитку малюків.",
                "Description for age 4-6 programs");

        createTextIfNotExists("AGE_6_10_MESSAGE",
                "🎒 *Програми для початкової школи (6-10 років)*\n\n" +
                        "Навчальні програми та додаткові заняття для учнів початкових класів.",
                "Description for age 6-10 programs");

        createTextIfNotExists("AGE_11_15_MESSAGE",
                "🧠 *Програми для середньої школи (11-15 років)*\n\n" +
                        "Спеціалізовані програми для підлітків та учнів середніх класів.",
                "Description for age 11-15 programs");

        createTextIfNotExists("AGE_15_18_MESSAGE",
                "🎯 *Програми для старшої школи (15-18 років)*\n\n" +
                        "Підготовка до вступу та спеціальні програми для старшокласників.",
                "Description for age 15-18 programs");

        createTextIfNotExists("SPECIALISTS_MESSAGE",
                "👨‍⚕️ *Спеціалісти*\n\n" +
                        "Професійна допомога від кваліфікованих спеціалістів.",
                "Description for specialists services");

        // Program details - Age 4-6
        createTextIfNotExists("PROGRAM_PRESCHOOL_DETAILS",
                "📚 *Підготовка до школи*\n\n" +
                        "🎯 *Мета програми:* Комплексна підготовка дитини до навчання в школі\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Навчання читанню та письму\n" +
                        "• Основи математики\n" +
                        "• Розвиток мовлення\n" +
                        "• Підготовка руки до письма\n" +
                        "• Розвиток логічного мислення\n\n" +
                        "⏰ *Тривалість:* 60 хвилин\n" +
                        "👥 *Група:* до 8 дітей\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for preschool preparation program");

        // Program details - Age 6-10
        createTextIfNotExists("PROGRAM_PRIMARY_DETAILS",
                "🏫 *Програма початкової школи*\n\n" +
                        "🎯 *Мета програми:* Допомога в освоєнні шкільної програми\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Допомога з домашніми завданнями\n" +
                        "• Поглиблене вивчення предметів\n" +
                        "• Розвиток навичок самостійного навчання\n" +
                        "• Індивідуальний підхід\n\n" +
                        "⏰ *Тривалість:* 90 хвилин\n" +
                        "👥 *Група:* до 6 дітей\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for primary school program");

        createTextIfNotExists("PROGRAM_ENGLISH_DETAILS",
                "🇬🇧 *Англійська мова*\n\n" +
                        "🎯 *Мета програми:* Вивчення англійської мови в ігровій формі\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Основи граматики\n" +
                        "• Розширення словникового запасу\n" +
                        "• Розвиток навичок говоріння\n" +
                        "• Ігрові методики навчання\n" +
                        "• Аудіювання та читання\n\n" +
                        "⏰ *Тривалість:* 60 хвилин\n" +
                        "👥 *Група:* до 8 дітей\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for English language program");

        createTextIfNotExists("PROGRAM_FINANCIAL_DETAILS",
                "💰 *Фінансова грамотність*\n\n" +
                        "🎯 *Мета програми:* Основи фінансової грамотності для дітей\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Поняття грошей та їх цінності\n" +
                        "• Планування витрат\n" +
                        "• Заощадження\n" +
                        "• Ігри з фінансової грамотності\n\n" +
                        "⏰ *Тривалість:* 45 хвилин\n" +
                        "👥 *Група:* до 10 дітей\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for financial literacy program");

        createTextIfNotExists("PROGRAM_CREATIVE_DETAILS",
                "🎨 *Творчі гуртки*\n\n" +
                        "🎯 *Мета програми:* Розвиток творчих здібностей дитини\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Малювання та ліплення\n" +
                        "• Аплікації та колажі\n" +
                        "• Вироби з природних матеріалів\n" +
                        "• Розвиток фантазії та уяви\n\n" +
                        "⏰ *Тривалість:* 60 хвилин\n" +
                        "👥 *Група:* до 8 дітей\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for creative programs");

        // Program details - Age 11-15
        createTextIfNotExists("PROGRAM_TEEN_PSYCHOLOGY_DETAILS",
                "🧠 *Психолог для підлітків*\n\n" +
                        "🎯 *Мета програми:* Психологічна підтримка підлітків\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Індивідуальні консультації\n" +
                        "• Групова терапія\n" +
                        "• Робота з емоціями\n" +
                        "• Розвиток соціальних навичок\n" +
                        "• Підготовка до дорослого життя\n\n" +
                        "⏰ *Тривалість:* 50 хвилин\n" +
                        "👥 *Формат:* індивідуально або в групі\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for teen psychology program");

        // Program details - Age 15-18
        createTextIfNotExists("PROGRAM_NMT_DETAILS",
                "🎯 *Підготовка до НМТ*\n\n" +
                        "🎯 *Мета програми:* Якісна підготовка до Національного мультипредметного тесту\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Математика\n" +
                        "• Українська мова та література\n" +
                        "• Іноземна мова\n" +
                        "• Пробні тестування\n" +
                        "• Індивідуальні консультації\n\n" +
                        "⏰ *Тривалість:* 120 хвилин\n" +
                        "👥 *Група:* до 12 учнів\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for NMT preparation program");

        // Specialists programs
        createTextIfNotExists("PROGRAM_PSYCHOLOGIST_DETAILS",
                "👩‍⚕️ *Психолог (4-18 років)*\n\n" +
                        "🎯 *Мета програми:* Професійна психологічна допомога дітям та підліткам\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Діагностика психологічного стану\n" +
                        "• Індивідуальна терапія\n" +
                        "• Сімейне консультування\n" +
                        "• Корекційна робота\n" +
                        "• Розвиток емоційного інтелекту\n\n" +
                        "⏰ *Тривалість:* 50 хвилин\n" +
                        "👥 *Формат:* індивідуально\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for psychologist services");

        createTextIfNotExists("PROGRAM_SPEECH_THERAPIST_DETAILS",
                "🗣️ *Логопед (4-10 років)*\n\n" +
                        "🎯 *Мета програми:* Корекція мовленнєвих порушень у дітей\n\n" +
                        "📋 *Що включає:*\n" +
                        "• Діагностика мовлення\n" +
                        "• Корекція звуковимови\n" +
                        "• Розвиток фонематичного слуху\n" +
                        "• Постановка звуків\n" +
                        "• Розвиток зв'язного мовлення\n\n" +
                        "⏰ *Тривалість:* 30-45 хвилин\n" +
                        "👥 *Формат:* індивідуально\n" +
                        "💰 *Вартість:* уточнюйте при записі",
                "Details for speech therapist services");

        // Static content
        createTextIfNotExists("FAQ_TEXT",
                "❓ *Часті запитання*\n\n" +
                        "*Q: Як записатися на заняття?*\n" +
                        "A: Зателефонуйте нам або напишіть у месенджер\n\n" +
                        "*Q: Які документи потрібні?*\n" +
                        "A: Достатньо копії свідоцтва про народження\n\n" +
                        "*Q: Чи є знижки?*\n" +
                        "A: Так, діють сімейні знижки та акції\n\n" +
                        "*Q: Коли можна відвідати пробне заняття?*\n" +
                        "A: Перше заняття завжди пробне та безкоштовне",
                "FAQ content");

        createTextIfNotExists("CONTACTS_TEXT",
                "📋 *Контакти та адреса*\n\n" +
                        "📍 *Адреса:* вул. Прикладна, 1, Київ\n\n" +
                        "📞 *Телефон:* +38 (050) 123-45-67\n\n" +
                        "✉️ *Email:* info@stezhka.ua\n\n" +
                        "🌐 *Сайт:* www.stezhka.ua\n\n" +
                        "🕐 *Режим роботи:*\n" +
                        "Пн-Пт: 9:00-20:00\n" +
                        "Сб: 9:00-17:00\n" +
                        "Нд: вихідний",
                "Contact information");

        createTextIfNotExists("NEWS_TEXT",
                "📢 *Новини та акції*\n\n" +
                        "🎉 *Нові групи:* Набір у вересні 2024\n\n" +
                        "💰 *Акція:* Знижка 20% при оплаті за півроку\n\n" +
                        "🎈 *Подія:* День відкритих дверей 15 травня\n\n" +
                        "📚 *Новина:* Запускаємо онлайн-курси\n\n" +
                        "🎁 *Бонус:* Безкоштовне пробне заняття для всіх нових учнів",
                "News and promotions");

        logger.info("Default text initialization completed");
    }

    private void createTextIfNotExists(String key, String value, String description) {
        if (!textContentRepository.existsByTextKey(key)) {
            TextContent content = new TextContent(key, value, description);
            textContentRepository.save(content);
            textCache.put(key, value);
        }
    }

    /**
     * Fallback texts in case database is unavailable
     */
    private void initializeFallbackTexts() {
        logger.info("Initializing fallback text values...");

        textCache.put("WELCOME_MESSAGE", "🌟 Вітаємо в Центрі «Стежка»! Оберіть дію нижче👇");
        textCache.put("MAIN_MENU_MESSAGE", "🏠 Головне меню\n\nОберіть розділ:");
        textCache.put("PROGRAMS_MENU_MESSAGE", "🎓 Навчальні програми\n\nОберіть вікову категорію:");
        logger.info("Fallback text initialization completed");
    }
}