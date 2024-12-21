package org.example.bot.processor.Edit;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработчик состояния редактирования ответа
 */
@Component
public class EditAnswerTextChoiceProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика редактирования формулировки ответа
     *
     * @param stateService   сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     */
    public EditAnswerTextChoiceProcessor(StateService stateService,
                                         ContextService contextService) {
        super("EDIT_ANSWER_TEXT_CHOICE");
        this.stateService = stateService;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<QuestionEntity> optionalCurrentQuestion = contextService.getCurrentQuestion(userId);
        if (optionalCurrentQuestion.isEmpty() || parts.length != 2) {
            return new BotResponse("Вопрос не найден");
        }

        QuestionEntity currentQuestion = optionalCurrentQuestion.get();
        int answerIndex = Integer.parseInt(parts[1]);
        if (answerIndex < 0 || answerIndex >= currentQuestion.getAnswers().size()) {
            return new BotResponse("Некорректный номер ответа. Попробуйте еще раз.");
        }

        contextService.setEditingAnswerIndex(userId, answerIndex);
        stateService.changeStateById(userId, UserState.EDIT_ANSWER_TEXT);
        return new BotResponse("Введите новую формулировку ответа");
    }
}
