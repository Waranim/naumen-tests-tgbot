package org.example.bot.processor.Add;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.service.QuestionService;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.springframework.stereotype.Component;

/**
 * Обработчик состояния добавления названия вопроса
 */
@Component
public class AddQuestionTextProcessor extends AbstractStateProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Сервис для управления тестами
     */
    private final QuestionService questionService;

    /**
     * Конструктор для инициализации обработчика добавления названия вопроса
     *
     * @param stateService    сервис для управления состояниями
     * @param contextService  сервис для управления контекстом
     * @param questionService сервис для управления тестами
     */
    public AddQuestionTextProcessor(StateService stateService,
                                    ContextService contextService,
                                    QuestionService questionService) {
        super(stateService, UserState.ADD_QUESTION_TEXT);
        this.stateService = stateService;
        this.contextService = contextService;
        this.questionService = questionService;
    }

    @Override
    public String process(Long userId, String message) {
        QuestionEntity currentQuestion = contextService.getCurrentQuestion(userId);
        currentQuestion.setQuestion(message);
        questionService.update(currentQuestion);
        stateService.changeStateById(userId, UserState.ADD_ANSWER);
        return "Введите вариант ответа. Если вы хотите закончить добавлять варианты, введите команду /stop.";
    }
}