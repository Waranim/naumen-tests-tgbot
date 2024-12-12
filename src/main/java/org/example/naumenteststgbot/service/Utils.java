package org.example.naumenteststgbot.service;

import org.example.naumenteststgbot.entity.AnswerEntity;
import org.example.naumenteststgbot.entity.QuestionEntity;
import org.example.naumenteststgbot.entity.TestEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Вспомогательный сервис
 */
@Service
public class Utils {

    private final UserService userService;

    public Utils(UserService userService) {
        this.userService = userService;
    }

    /**
     * Получить развернутое строковое представление сущности теста
     */
    public String testToString(TestEntity test) {
        List<QuestionEntity> questions = test.getQuestions();
        StringBuilder response = new StringBuilder(String.format("Тест “%s”. Всего вопросов: %s\n",  test.getTitle(), questions.size()));
        String correctAnswerPercent = test.getCountAnsweredQuestionsAllUsers() != 0
                ? String.valueOf((test.getCorrectAnswerCountAllUsers() * 100) / test.getCountAnsweredQuestionsAllUsers())
                : "Тест ещё не проходили";
        response.append(
                String.format("\nСтатистика по тесту:\nОбщее количество попыток: %d\nСредний процент правильных ответов: %s\nКоличество пользователей с доступом к тесту: %d\n\n",
                        test.getCountTries(), correctAnswerPercent, test.getRecipients().size()));
        for (QuestionEntity question : questions) {
            response.append("Вопрос: %s\nВарианты ответов:\n".formatted(question.getQuestion()));
            List<AnswerEntity> answers = question.getAnswers();
            AnswerEntity correctAnswer = null;
            for (int i = 0; i < answers.size(); i++) {
                var answer = answers.get(i);
                response.append("%s - %s\n".formatted(i+1, answer.getAnswerText()));
                if(answer.isCorrect()) correctAnswer = answer;
            }
            response.append("Правильный вариант: ").append(Objects.requireNonNull(correctAnswer).getAnswerText()).append("\n\n");
        }
        return response.toString();
    }

    /**
     * Получить строковое представление списка тестов
     */
    public String testsListToString(List<TestEntity> tests) {
        StringBuilder response = new StringBuilder();
        for(int i = 0; i < tests.size(); i++) {
            TestEntity currentTest = tests.get(i);
            response.append(String.format("%s)  id: %s %s\n", i+1, currentTest.getId(), currentTest.getTitle()));
        }
        return response.toString();
    }

    /**
     * Получить строковое представление списка тестов с добавлением никнейма создателя,
     * если идентификатор пользователя не совпадает с идентификатором создателя
     */
    public String testsListToString(List<TestEntity> tests, Long userId) {
        StringBuilder response = new StringBuilder();
        for(int i = 0; i < tests.size(); i++) {
            TestEntity currentTest = tests.get(i);
            String creatorUsername = userId.equals(currentTest.getCreatorId())
                    ? ""
                    : " (%s)".formatted(userService.getUserById(currentTest.getCreatorId()).getUsername());
            response.append(String.format("%s)  id: %s %s" + creatorUsername + "\n", i+1, currentTest.getId(), currentTest.getTitle()));
        }
        return response.toString();
    }

    /**
     * Узнать, находится ли в строке только лишь число
     * @return true - если только цифры в строке, false - все остальные случаи.
     */
    public boolean isNumber(String number) {
        return number.matches("^-?\\d+$");
    }
}
