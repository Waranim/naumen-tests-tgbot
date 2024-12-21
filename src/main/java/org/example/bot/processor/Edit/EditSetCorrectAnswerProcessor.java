package org.example.bot.processor.Edit;

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
 * Обработчик состояния редактирования правильного ответа
 */
@Component
public class EditSetCorrectAnswerProcessor extends AbstractCallbackProcessor {

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
     * Конструктор для инициализации обработчика редактирования правильного ответа
     *
     * @param stateService    сервис для управления состояниями
     * @param contextService  сервис для управления контекстом
     * @param questionService сервис для управления вопросами
     */
    public EditSetCorrectAnswerProcessor(StateService stateService,
                                         ContextService contextService,
                                         QuestionService questionService) {
        super("SET_CORRECT_ANSWER");
        this.stateService = stateService;
        this.contextService = contextService;
        this.questionService = questionService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<QuestionEntity> optionalCurrentQuestion = contextService.getCurrentQuestion(userId);
        if (optionalCurrentQuestion.isEmpty() || parts.length != 2) {
            return new BotResponse("Вопрос не найден");
        }
        QuestionEntity currentQuestion = optionalCurrentQuestion.get();
        try {
            int optionIndex = Integer.parseInt(parts[1]) + 1;
            questionService.setCorrectAnswer(currentQuestion, optionIndex);
            stateService.changeStateById(userId, UserState.DEFAULT);
            return new BotResponse(String.format("Вариант ответа %s назначен правильным.", optionIndex));
        } catch (NumberFormatException e) {
            return new BotResponse("Некорректный формат ввода. Пожалуйста, введите число.");
        } catch (IllegalArgumentException e) {
            return new BotResponse(String.format("Некорректный номер варианта ответа. Введите число от 1 до %d",
                    currentQuestion.getAnswers().size()));
        }
    }
}
