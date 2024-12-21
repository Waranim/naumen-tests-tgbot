package org.example.bot.processor;

import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;


/**
 * Обработка игнорирования действия пользователя
 */
@Component
public class IgnoreProcessor extends AbstractCallbackProcessor {

    /**
     * Конструктор для инициализации обработчика callback.
     */
    protected IgnoreProcessor() {
        super("IGNORE");
    }

    @Override
    public BotResponse process(Long userId, String callback) {
        return new BotResponse("");
    }
}
