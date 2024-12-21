package org.example.bot.processor;


import org.example.bot.telegram.BotResponse;

/**
 * Обработчик сообщений
 */
public interface MessageProcessor {
    
    /**
     * Может ли обработать сообщение
     */
    boolean canProcess(Long userId, String message);

    /**
     * Обработать сообщение
     * @param userId идентификатор пользователя
     * @param message сообщение
     * @return Ответ на сообщение
     */
    BotResponse process(Long userId, String message);
}