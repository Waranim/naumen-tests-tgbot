package org.example.bot.processor.Del;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.QuestionService;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработчик состояния подтверждения удаления вопроса
 */
@Component
public class ConfirmDelQuestion extends AbstractCallbackProcessor {

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
     * Конструктор для инициализации обработчика подтверждения удаления теста.
     *
     * @param stateService    сервис для управления состояниями
     * @param contextService  сервис для управления контекстом
     * @param questionService сервис для управления вопросами
     */
    public ConfirmDelQuestion(StateService stateService,
                              ContextService contextService,
                              QuestionService questionService) {
        super("DEL_QUESTION_CONFIRM");
        this.stateService = stateService;
        this.contextService = contextService;
        this.questionService = questionService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<QuestionEntity> optionalCurrentQuestion = contextService.getCurrentQuestion(userId);
        if (optionalCurrentQuestion.isEmpty()) {
            return new BotResponse("Вопрос не найден");
        }

        QuestionEntity currentQuestion = optionalCurrentQuestion.get();
        if (currentQuestion.getId() == Long.parseLong(parts[1])) {
            stateService.changeStateById(userId, UserState.DEFAULT);
            if (!parts[2].equals("YES")) {
                return new BotResponse(String.format("Вопрос “%s” из теста “%s” не удален.",
                        currentQuestion.getQuestion(), currentQuestion.getTest().getTitle()));
            }
            contextService.setCurrentQuestion(userId, null);
            questionService.delete(currentQuestion);
            return new BotResponse(String.format("Вопрос “%s” из теста “%s” удален.",
                    currentQuestion.getQuestion(), currentQuestion.getTest().getTitle()));
        }
        return new BotResponse("");
    }
}
