package org.example.bot.processor.passage;

import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработка завершения теста
 */
@Component
public class FinishTestProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика callback.
     */
    public FinishTestProcessor(StateService stateService, ContextService contextService) {
        super("FINISH_TEST");
        this.stateService = stateService;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        Optional<UserState> currentStateOpt = stateService.getCurrentState(userId);
        if (currentStateOpt.isEmpty() || !currentStateOpt.get().equals(UserState.PASSAGE_TEST)) {
            return new BotResponse("");
        }
        stateService.changeStateById(userId, UserState.DEFAULT);
        contextService.setCurrentTest(userId, null);
        contextService.setCurrentQuestion(userId, null);
        Optional<Integer> correctAnswerCountOptional = contextService.getCorrectAnswerCount(userId);
        Optional<Integer> countAnsweredQuestionsOptional = contextService.getCountAnsweredQuestions(userId);

        if (correctAnswerCountOptional.isEmpty() || countAnsweredQuestionsOptional.isEmpty()) {
            return new BotResponse("Проходимый тест не найден");
        }

        Integer correctAnswerCount = correctAnswerCountOptional.get();
        Integer countAnsweredQuestions = countAnsweredQuestionsOptional.get();
        Integer correctAnswerPercent = countAnsweredQuestions != 0
                ? (correctAnswerCount * 100) / countAnsweredQuestions
                : 0;

        String text = "Тест завершен!\n" +
                String.format("Правильных ответов: %d/%d\n", correctAnswerCount, countAnsweredQuestions) +
                String.format("Процент правильных ответов: %d%%", correctAnswerPercent);

        return new BotResponse(text);
    }
}
