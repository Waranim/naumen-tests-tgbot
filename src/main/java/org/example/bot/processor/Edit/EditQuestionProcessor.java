package org.example.bot.processor.Edit;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик состояния редактирования вопроса
 */
@Component
public class EditQuestionProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;

    /**
     * Конструктор для инициализации обработчика редактирования вопроса
     *
     * @param stateService сервис для управления состояниями
     */
    public EditQuestionProcessor(StateService stateService) {
        super("EDIT_QUESTION");
        this.stateService = stateService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        long questionId = Long.parseLong(parts[1]);
        List<List<InlineButtonDTO>> buttons = new ArrayList<>();
        buttons.add(List.of(new InlineButtonDTO("Изменить формулировку ответа",
                "EDIT_ANSWER_OPTION_CHOICE " + questionId + " 1")));
        buttons.add(List.of(new InlineButtonDTO("Изменить правильность варианта ответа",
                "EDIT_ANSWER_OPTION_CHOICE " + questionId + " 2")));
        if (parts[2].equals("1")) {
            stateService.changeStateById(userId, UserState.EDIT_QUESTION_TEXT);
            return new BotResponse("Введите новый текст вопроса");
        } else if (parts[2].equals("2")) {
            stateService.changeStateById(userId, UserState.EDIT_ANSWER_OPTION_CHOICE);
            return new BotResponse(
                    "Что вы хотите сделать с вариантом ответа?\n",
                    buttons,
                    false);
        }

        return new BotResponse("Некорректный ввод");
    }
}
