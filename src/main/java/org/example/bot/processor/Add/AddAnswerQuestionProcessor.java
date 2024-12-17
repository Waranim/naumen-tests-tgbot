package org.example.bot.processor.Add;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.processor.StopCommandProcessor;
import org.example.bot.service.QuestionService;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.springframework.stereotype.Component;

/**
 * Обработчик состояния добавления ответа в вопрос
 */
@Component
public class AddAnswerQuestionProcessor extends AbstractStateProcessor {

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Сервис для управления вопросами
     */
    private final QuestionService questionService;

    /**
     * Обработчик команды /stop
     */
    private final StopCommandProcessor stopCommandProcessor;

    /**
     * Конструктор для инициализации обработчика состояния добавления ответа в вопрос
     *
     * @param stateService         сервис для управления состоянием
     * @param contextService       сервис для управления контекстом
     * @param questionService      сервис для управления вопросами
     * @param stopCommandProcessor обработчик команды /stop
     */
    public AddAnswerQuestionProcessor(StateService stateService,
                                      ContextService contextService,
                                      QuestionService questionService, StopCommandProcessor stopCommandProcessor) {
        super(stateService, UserState.ADD_ANSWER);
        this.contextService = contextService;
        this.questionService = questionService;
        this.stopCommandProcessor = stopCommandProcessor;
    }

    @Override
    public String process(Long userId, String message) {
        if (message.equals("/stop")) {
            return stopCommandProcessor.process(userId, message);
        }
        QuestionEntity currentQuestion = contextService.getCurrentQuestion(userId);
        questionService.addAnswerOption(currentQuestion, message);
        return "Введите вариант ответа. Если вы хотите закончить добавлять варианты, введите команду “/stop”.";
    }
}