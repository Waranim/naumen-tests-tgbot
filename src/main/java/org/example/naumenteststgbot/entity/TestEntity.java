package org.example.naumenteststgbot.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность теста
 */
@Entity
public class TestEntity extends BaseEntity {
    /**
     * Создатель теста
     */
    private Long creatorId;

    /**
     * Вопросы в тесте
     */
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionEntity> question = new ArrayList<>();

    /**
     * Название теста
     */
    private String title;

    /**
     * Описание теста
     */
    private String description;

    /**
     * Открытие-закрытие доступа к тесту
     */
    private boolean accessOpen;

    /**
     * Количество попыток прохождения
     */
    private Integer countTries;

    /**
     * Количество правильных ответов в тесте всех пользователей
     */
    private Integer correctAnswerCountAllUsers;

    /**
     * Количество решённых вопросов в тесте всех пользователей
     */
    private Integer countAnsweredQuestionsAllUsers;

    /**
     * Пользователи, которые получили доступ к данному тесту
     */
    @ManyToMany(fetch = FetchType.EAGER)
    private final List<UserEntity> recipients = new ArrayList<>();
    public TestEntity() {
    }
    public TestEntity(Long creatorId) {
        this.creatorId = creatorId;
        this.accessOpen = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<QuestionEntity> getQuestions() {
        return question;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Integer getCountTries() {
        return countTries;
    }

    public void setCountTries(Integer countTries) {
        this.countTries = countTries;
    }

    public Integer getCorrectAnswerCountAllUsers() {
        return correctAnswerCountAllUsers;
    }

    public void setCorrectAnswerCountAllUsers(Integer correctAnswerCountAllUsers) {
        this.correctAnswerCountAllUsers = correctAnswerCountAllUsers;
    }

    public Integer getCountAnsweredQuestionsAllUsers() {
        return countAnsweredQuestionsAllUsers;
    }

    public void setCountAnsweredQuestionsAllUsers(Integer countAnsweredQuestionsAllUsers) {
        this.countAnsweredQuestionsAllUsers = countAnsweredQuestionsAllUsers;
    }

    /**
     * Вернуть значение состояния доступа
     */
    public boolean isAccessOpen() {
        return accessOpen;
    }

    /**
     * Установить значение состояния доступа
     */
    public void setAccessOpen(boolean accessOpen) {
        this.accessOpen = accessOpen;
    }

    public List<UserEntity> getRecipients() {
        return recipients;
    }
}
