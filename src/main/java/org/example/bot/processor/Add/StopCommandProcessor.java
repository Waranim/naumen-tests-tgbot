package org.example.bot.processor.Add;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.UserContext;
import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;


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
     * Конструктор для инициализации обработчика команды /stop
     *
     * @param stateService   сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     */
    public StopCommandProcessor(StateService stateService,
                                ContextService contextService) {
        super("/stop");
        this.stateService = stateService;
        this.contextService = contextService;
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
        List<List<InlineButtonDTO>> buttons = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answer = answers.get(i);
            String text = answer.getAnswerText();
            buttons.add(List.of(new InlineButtonDTO(text, "SET_CORRECT_ANSWER " + i)));
        }
        return new BotResponse(
                "Укажите правильный вариант ответа:\n" + answersToString(currentQuestion.getAnswers()),
                buttons,
                false);
    }

    /**
     * Преобразует список ответов у вопроса в строку
     */
    private String answersToString(List<AnswerEntity> answers) {
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answer = answers.get(i);
            response.append(String.format("%d: %s%s\n", i + 1,
                    answer.getAnswerText(),
                    answer.isCorrect() ? " (верный)" : ""));
        }
        return response.toString();
    }
}