package org.example.bot.telegram;

import org.example.bot.dto.InlineButtonDTO;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Ответ бота
 */
public class BotResponse {

    /**
     * Сообщение ответа
     */
    private final String message;

    /**
     * Inline кнопки
     */
    private final List<List<InlineButtonDTO>> buttons;

    /**
     * Редактировать ли сообщение
     */
    private final boolean isEdit;

    /**
     * Конструктор для ответа бота
     */
    public BotResponse(String message, List<List<InlineButtonDTO>> buttons, boolean isEdit) {
        this.message = message;
        this.buttons = buttons;
        this.isEdit = isEdit;
    }

    /**
     * Конструктор только с сообщением
     */
    public BotResponse(String message) {
        this.message = message;
        this.buttons = null;
        this.isEdit = false;
    }

    /**
     * Получить кнопки
     *
     * @return возвращает кнопки
     */
    public List<List<InlineButtonDTO>> getButtons() {
        return buttons;
    }

    /**
     * Получить сообщение
     */
    public String getMessage() {
        return message;
    }

    /**
     * Редактировать ли сообщение
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * Преобразует BotResponse в соответствующий метод Telegram API
     *
     * @param chatId ID чата
     * @param messageId ID сообщения (для редактирования)
     * @return SendMessage или EditMessageText в зависимости от isEdit
     */
    public BotApiMethod<?> convertToMessage(String chatId, Integer messageId) {
        if (message.isBlank()) {
            return null;
        }

        InlineKeyboardMarkup markup = getInlineKeyboardMarkup();

        if (isEdit && messageId != null) {
            return EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(message)
                    .replyMarkup(markup)
                    .build();
        } else {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .replyMarkup(markup)
                    .build();
        }
    }

    /**
     * Создаёт инлайн клавиатуру
     */
    private InlineKeyboardMarkup getInlineKeyboardMarkup() {
        if (buttons == null || buttons.isEmpty()) {
            return null;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (List<InlineButtonDTO> buttonsRow : buttons) {
            List<InlineKeyboardButton> row = buttonsRow.stream()
                    .map(button -> {
                        InlineKeyboardButton inlineButton = new InlineKeyboardButton(button.text());
                        inlineButton.setCallbackData(button.callbackData());
                        return inlineButton;
                    })
                    .toList();
            keyboard.add(row);
        }

        markup.setKeyboard(keyboard);
        return markup;
    }
}
