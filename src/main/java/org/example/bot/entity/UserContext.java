package org.example.bot.entity;

import jakarta.persistence.*;
import org.example.bot.state.UserState;

/**
 * Контекст пользователя
 */
@Entity
public class UserContext extends BaseEntity {

    /**
     * Идентификатор пользователя
     */
    private Long userId;

    /**
     * Состояние пользователя
     */
    @Enumerated(EnumType.STRING)
    private UserState state = UserState.DEFAULT;

    /**
     * Тест, который в данный момент использует пользователь
     */
    @OneToOne
    private TestEntity currentTest;

    /**
     * Индекс редактируемого варианта ответа
     */
    private Integer editingAnswerIndex;

    /**
     * Вопрос, который в данный момент использует пользователь
     */
    @OneToOne(cascade = CascadeType.ALL)
    private QuestionEntity currentQuestion;

    /**
     * Количество правильных ответов в тесте
     */
    private Integer correctAnswerCount;

    /**
     * Количество решённых вопросов в тесте
     */
    private Integer countAnsweredQuestions;

    /**
     * Получить идентификатор пользователя
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Получить текущее состояние пользователя
     */
    public UserState getState() {
        return state;
    }

    /**
     * Установить текущее состояние пользователя
     */
    public void setState(UserState state) {
        this.state = state;
    }

    /**
     * Получить тест, который в данный момент используется пользователем
     */
    public TestEntity getCurrentTest() {
        return currentTest;
    }

    /**
     * Установить тест, который будет использоваться пользователем
     */
    public void setCurrentTest(TestEntity currentTest) {
        this.currentTest = currentTest;
    }

    /**
     * Получить вопрос, который в данный момент используется пользователем
     */
    public QuestionEntity getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * Установить вопрос, который будет использоваться пользователем
     */
    public void setCurrentQuestion(QuestionEntity currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    /**
     * Конструктор без параметров
     */
    public UserContext() {
    }

    /**
     * Конструктор с указанием идентификатора пользователя
     */
    public UserContext(Long userId) {
        this.userId = userId;
    }

    /**
     * Получить индекс редактируемого пользователем варианта ответа
     */
    public Integer getEditingAnswerIndex() {
        return editingAnswerIndex;
    }

    /**
     * Установить индекс редактируемого пользователем варианта ответа
     */
    public void setEditingAnswerIndex(Integer editingAnswerIndex) {
        this.editingAnswerIndex = editingAnswerIndex;
    }

    /**
     * Получить количество правильных ответов в тесте
     */
    public Integer getCorrectAnswerCount() {
        return correctAnswerCount;
    }

    /**
     * Установить количество правильных ответов в тесте
     */
    public void setCorrectAnswerCount(Integer correctAnswerCount) {
        this.correctAnswerCount = correctAnswerCount;
    }

    /**
     * Получить количество решённых вопросов в тесте
     */
    public Integer getCountAnsweredQuestions() {
        return countAnsweredQuestions;
    }

    /**
     * Установить количество решённых вопросов в тесте
     */
    public void setCountAnsweredQuestions(Integer countAnsweredQuestions) {
        this.countAnsweredQuestions = countAnsweredQuestions;
    }
}
