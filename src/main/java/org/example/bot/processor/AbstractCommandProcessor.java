package org.example.bot.processor;

/**
 * Абстрактный класс для обработки команд.
 */
public abstract class AbstractCommandProcessor implements MessageProcessor {
    /**
     * Команда, которую обрабатывает данный класс.
     */
    private final String command;

    /**
     * Конструктор для инициализации обработчика команды.
     * 
     * @param command команда, которую будет обрабатывать данный класс
     */
    protected AbstractCommandProcessor(String command) {
        this.command = command;
    }

    @Override
    public final boolean canProcess(Long userId, String message) {
        return extractCommand(message).equals(command);
    }

    /**
     * Извлечь команду из сообщения
     */
    private String extractCommand(String message) {
        int spaceIndex = message.indexOf(' ');
        return spaceIndex == -1 ? message : message.substring(0, spaceIndex);
    }
}