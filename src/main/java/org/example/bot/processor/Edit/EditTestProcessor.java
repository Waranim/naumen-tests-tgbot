package org.example.bot.processor.Edit;

import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

/**
 * Обработчик состояния редактирования теста.
 */
@Component
public class EditTestProcessor extends AbstractCallbackProcessor {
    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;

    /**
     * Конструктор для инициализации обработчика редактирования теста.
     * 
     * @param stateService сервис для управления состояниями
     */
    public EditTestProcessor(StateService stateService) {
        super("EDIT_TEST");
        this.stateService = stateService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        if (parts[2].equals("EDIT_TEST_TITLE")) {
            stateService.changeStateById(userId, UserState.EDIT_TEST_TITLE);
            return new BotResponse("Введите новое название теста");
        } else if (parts[2].equals("EDIT_TEST_DESCRIPTION")) {
            stateService.changeStateById(userId, UserState.EDIT_TEST_DESCRIPTION);
            return new BotResponse("Введите новое описание теста");
        }

        return new BotResponse("Некорректный ввод");
    }
}
