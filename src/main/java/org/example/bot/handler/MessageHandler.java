package org.example.bot.handler;

import org.example.bot.processor.MessageProcessor;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Обработчик сообщений
 */
@Component
public class MessageHandler {
    /**
     * Список обработчиков сообщений.
     */
    private final List<MessageProcessor> processors;

    /**
     * Конструктор для инициализации обработчика сообщений.
     * 
     * @param processors список обработчиков сообщений
     */
    public MessageHandler(List<MessageProcessor> processors) {
        this.processors = processors;
    }
    /**
     * Обрабатывает входящее сообщение от пользователя
     * 
     * @param message сообщение от пользователя
     * @param userId идентификатор пользователя
     * @return ответное сообщение для пользователя
     */
    public BotResponse handle(String message, Long userId) {
        return processors.stream()
                .filter(processor -> processor.canProcess(userId, message))
                .findFirst()
                .map(processor -> processor.process(userId, message))
                .orElse(new BotResponse("Я вас не понимаю, для справки используйте /help"));
    }
}
