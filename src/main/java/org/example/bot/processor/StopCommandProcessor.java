package org.example.bot.processor;

import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.UserContext;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.AnswerUtils;
import org.example.bot.util.ButtonUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /stop
 */
@Component
public class StopCommandProcessor extends AbstractCommandProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Утилита с вспомогательными методами для вопросов
     */
    private final AnswerUtils answerUtils;
    private final ButtonUtils buttonUtils;

    /**
     * Конструктор для инициализации обработчика команды /stop
     *
     * @param stateService   сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     * @param answerUtils    утилита с вспомогательными методами для вопросов
     */
    public StopCommandProcessor(StateService stateService,
                                ContextService contextService,
                                AnswerUtils answerUtils, ButtonUtils buttonUtils) {
        super("/stop");
        this.stateService = stateService;
        this.contextService = contextService;
        this.answerUtils = answerUtils;
        this.buttonUtils = buttonUtils;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        Optional<UserContext> optionalContext = contextService.getContext(userId);
        if (optionalContext.isEmpty()) {
            return new BotResponse("Ошибка");
        }
        UserState userState = optionalContext.get().getState();
        Optional<QuestionEntity> optionalCurrentQuestion = contextService.getCurrentQuestion(userId);
        if (optionalCurrentQuestion.isEmpty()) {
            return new BotResponse("Нет текущего вопроса. Пожалуйста, выберите или создайте вопрос.");
        }
        QuestionEntity currentQuestion = optionalCurrentQuestion.get();
        List<AnswerEntity> answers = currentQuestion.getAnswers();
        if (userState != UserState.ADD_ANSWER) {
            return new BotResponse("Команда /stop используется только при создании вопроса.");
        }
        if (answers.size() < 2) {
            return new BotResponse("Вы не создали необходимый минимум ответов (минимум: 2). Введите варианты ответа.");
        }
        stateService.changeStateById(userId, UserState.SET_CORRECT_ANSWER);
        List<InlineKeyboardButton> button = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answer = answers.get(i);
            String text = answer.getAnswerText();
            button.add(buttonUtils.createButton(text, "SET_CORRECT_ANSWER " + i));
        }
        return new BotResponse("Укажите правильный вариант ответа:\n" + answerUtils.answersToString(currentQuestion.getAnswers()), button, false);
    }
}