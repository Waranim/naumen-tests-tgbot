package org.example.bot.telegram;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.bot.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Телеграм бот
 */
@Component
class TelegramBot extends TelegramLongPollingBot {

    /**
     * Логер
     */
    private final Logger log = LogManager.getLogger(TelegramBot.class);

    /**
     * Имя бота
     */
    @Value("${bot.name}")
    private String name;

    /**
     * Обработчик сообщений
     */
    private final MessageHandler messageHandler;

    /**
     * Конструктор для инициализации бота
     * @param messageHandler обработчик сообщений
     * @param token токен бота
     */
    public TelegramBot(MessageHandler messageHandler, @Value("${bot.token}") String token) {
        super(token);
        this.messageHandler = messageHandler;
    }

    /**
     * Обработка входящих обновлений от Telegram API
     * @param update объект, содержащий информацию о взаимодействии пользователя с ботом (сообщения, callback и др.)
     */
    @Transactional
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            User user = update.getMessage().getFrom();
            Integer messageId = update.getMessage().getMessageId();
            Long userId = user.getId();
            String username = user.getUserName();
            String chatId = update.getMessage().getChatId().toString();
            if (message.equals("/start"))
                message += " " + username;
            sendMessage(messageHandler.handle(message, userId).convertToMessage(chatId, messageId));

        } else if (update.hasCallbackQuery()) {
            Long userId = update.getCallbackQuery().getFrom().getId();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String message = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            sendMessage(messageHandler.handle(message, userId).convertToMessage(chatId, messageId));
        }
    }

    /**
     * Получение имени бота
     * @return имя бота из конфигурации
     */
    @Override
    public String getBotUsername() {
        return name;
    }

    /**
     * Отправка сообщений
     */
    private void sendMessage(BotApiMethod<?> message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Не удалось отправить сообщение: {}", e.getMessage());
            }
        }
    }

    /**
     * Инициализирует телеграм бота
     */
    @PostConstruct
    private void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Бот не запущен", e);
        }
    }
}
