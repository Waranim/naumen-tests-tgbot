package org.example.bot.processor.Del;

import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

/**
 * Обработчик команды удаления вопроса
 */
@Component
public class DelQuestionCommandProcessor extends AbstractCommandProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Обработчик удаления вопроса
     */
    private final DelQuestionProcessor delQuestionProcessor;

    /**
     * Конструктор для инициализации обработчика команды удаления теста.
     *
     * @param stateService    сервис для управления состояниями
     * @param delQuestionProcessor обработчик удаления вопроса
     */
    public DelQuestionCommandProcessor(StateService stateService,
                                       DelQuestionProcessor delQuestionProcessor) {
        super("/del_question");
        this.stateService = stateService;
        this.delQuestionProcessor = delQuestionProcessor;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        if (parts.length == 2) {
            return delQuestionProcessor.process(userId, parts[1]);
        }
        if (parts.length == 1) {
            stateService.changeStateById(userId, UserState.DELETE_QUESTION);
            return new BotResponse("Введите id вопроса для удаления:\n");
        }
        return new BotResponse("Ошибка ввода. Укажите корректный id теста.");
    }
}
