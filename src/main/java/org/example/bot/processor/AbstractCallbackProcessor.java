package org.example.bot.processor;

/**
 * Абстрактный класс для обработки callback.
 */
public abstract class AbstractCallbackProcessor implements MessageProcessor {

    /**
     * Callback, который обрабатывает данный класс.
     */
    private final String callback;

    /**
     * Конструктор для инициализации обработчика callback.
     */
    protected AbstractCallbackProcessor(String callback) {
        this.callback = callback;
    }

    @Override
    public final boolean canProcess(Long userId, String message) {
        return message.startsWith(callback);
    }
}