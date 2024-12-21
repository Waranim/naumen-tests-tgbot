package org.example.bot.processor;

import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

/**
 * Обработчик команды справки.
 */
@Component
public class HelpCommandProcessor extends AbstractCommandProcessor {
    /**
     * Конструктор для инициализации обработчика команды справки.
     */
    public HelpCommandProcessor() {
        super("/help");
    }

    @Override
    public BotResponse process(Long userId, String message) {
        return new BotResponse("""
                Здравствуйте. Я бот специализирующийся на создании и прохождении тестов. Доступны следующие команды:
                /add – Добавить тест
                /add_question [testID] - Добавить вопрос к тесту
                /view – Посмотреть список тестов
                /view [testID] - Посмотреть тест
                /test - Начать прохождение теста
                /view_question [testID] - Посмотреть список вопросов к тесту
                /edit [testID] - Изменить тест с номером testID
                /edit_question [questionID] - Изменить вопрос с номером questionID
                /del [testID] – Удалить тест с номером testID
                /del_question [questionID] - Удалить вопрос с номером questionID
                /stop - Закончить ввод вариантов ответа, если добавлено минимум 2 варианта \
                при выполнении команды /add_question [testID]
                /help - Справка""");
    }
}
