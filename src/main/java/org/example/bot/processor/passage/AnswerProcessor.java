package org.example.bot.processor.passage;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.ButtonUtils;
import org.example.bot.util.TestUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Обработка ответа на вопрос
 */
@Component
public class AnswerProcessor extends AbstractCallbackProcessor {
    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Утилита для кнопок
     */
    private final ButtonUtils buttonUtils;

    /**
     * Утилита для тестов
     */
    private final TestUtils testUtils;

    /**
     * Конструктор для инициализации обработчика callback.
     */
    public AnswerProcessor(ContextService contextService,
                           StateService stateService,
                           ButtonUtils buttonUtils,
                           TestUtils testUtils) {
        super("ANSWER_QUESTION");
        this.contextService = contextService;
        this.stateService = stateService;
        this.buttonUtils = buttonUtils;
        this.testUtils = testUtils;
    }

    @Override
    public BotResponse process(Long userId, String callback) {
        String[] parts = callback.split(" ");
        Optional<UserState> currentStateOpt = stateService.getCurrentState(userId);
        if (currentStateOpt.isEmpty() || !currentStateOpt.get().equals(UserState.PASSAGE_TEST)) {
            return new BotResponse("");
        }

        Optional<TestEntity> test = contextService.getCurrentTest(userId);
        Optional<QuestionEntity> previousQuestionOpt = contextService.getCurrentQuestion(userId);
        if (test.isEmpty() || previousQuestionOpt.isEmpty() || parts.length != 2)
            return new BotResponse("Произошла ошибка при прохождении теста");

        String answer = parts[1];
        QuestionEntity previousQuestion = previousQuestionOpt.get();
        List<QuestionEntity> questions = test.get().getQuestions();
        int questionIndex = questions.indexOf(previousQuestion);
        List<InlineButtonDTO> buttons = new ArrayList<>();
        boolean isCompleted = questionIndex + 1 >= questions.size();
        String questionText = testUtils.createTextQuestion(questionIndex, questions);

        List<AnswerEntity> answers = previousQuestion.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answerQuestion = answers.get(i);
            String postfix = "";
            if (answerQuestion.getAnswerText().equals(answer)) {
                postfix = answerQuestion.isCorrect() ? " ✅" : " ❌";
                if (answerQuestion.isCorrect()) {
                    contextService.incrementCorrectAnswerCount(userId);
                }
            }
            int numberAnswer = i + 1;
            buttons.add(new InlineButtonDTO(numberAnswer + postfix, "IGNORE"));
        }

        contextService.incrementCountAnsweredQuestions(userId);

        return new BotResponse(questionText,
                buttonUtils.createKeyboardForTest(
                        contextService.getCorrectAnswerCount(userId).orElse(-1),
                        buttons,
                        "",
                        isCompleted
                ),
                true);
    }
}
