package org.example.bot.util;

import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Утилитарный класс для тестов
 */
@Component
public class TestUtils {

    /**
     * Получить строковое представление списка тестов
     */
    public String testsToString(List<TestEntity> tests) {
        StringBuilder response = new StringBuilder();
        for(int i = 0; i < tests.size(); i++) {
            TestEntity currentTest = tests.get(i);
            response.append(String.format("%s)  id: %s %s\n", i+1,
                    currentTest.getId(),
                    currentTest.getTitle()));
        }
        return response.toString();
    }

    /**
     * Создать текст вопроса для сообщения
     */
    public String createTextQuestion(int currentQuestionIndex, List<QuestionEntity> questions) {
        QuestionEntity question = questions.get(currentQuestionIndex);
        List<String> answers = question.getAnswers().stream().map(AnswerEntity::getAnswerText).toList();

        String textQuestion = String.format("Вопрос %d/%d: %s\nВарианты ответа:\n", currentQuestionIndex + 1, questions.size(), question.getQuestion());
        StringBuilder stringBuilder = new StringBuilder(textQuestion);
        for (int i = 0; i < answers.size(); i++) {
            stringBuilder.append(String.format("%d: %s\n", i + 1, answers.get(i)));
        }

        stringBuilder.append("\nВыберите один вариант ответа:");

        return stringBuilder.toString();
    }
}
