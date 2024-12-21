package org.example.bot.processor.Edit;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.service.QuestionService;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработчик состояния редактирования формулировки ответа
 */
@Component
public class EditAnswerTextProcessor extends AbstractStateProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Сервис для управления вопросами
     */
    private final QuestionService questionService;

    /**
     * Конструктор для инициализации обработчика редактирования формулировки ответа
     *
     * @param stateService    cервис для управления состояниями
     * @param contextService  cервис для управления контекстом
     * @param questionService cервис для управления вопросами
     */
    public EditAnswerTextProcessor(StateService stateService,
                                   ContextService contextService,
                                   QuestionService questionService) {
        super(stateService, UserState.EDIT_ANSWER_TEXT);
        this.stateService = stateService;
        this.contextService = contextService;
        this.questionService = questionService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        Optional<QuestionEntity> optionalCurrentQuestion = contextService.getCurrentQuestion(userId);
        Optional<Integer> optionalEditingAnswerIndex = contextService.getEditingAnswerIndex(userId);
        if (optionalCurrentQuestion.isEmpty() || optionalEditingAnswerIndex.isEmpty()) {
            return new BotResponse("Вопрос не найден");
        }

        QuestionEntity currentQuestion = optionalCurrentQuestion.get();
        int editingAnswerIndex = optionalEditingAnswerIndex.get();
        currentQuestion.getAnswers().get(editingAnswerIndex).setAnswerText(message);
        questionService.update(currentQuestion);
        stateService.changeStateById(userId, UserState.DEFAULT);

        return new BotResponse(String.format("Формулировка изменена на “%s”", message));
    }
}
