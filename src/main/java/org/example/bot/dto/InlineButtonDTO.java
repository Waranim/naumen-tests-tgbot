package org.example.bot.dto;

/**
 * DTO для inline кнопок
 * @param text - текст кнопки
 * @param callbackData - callback данные кнопки
 */
public record InlineButtonDTO(String text, String callbackData) {
}
