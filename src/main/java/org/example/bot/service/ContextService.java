package org.example.bot.service;

import org.example.bot.entity.*;
import org.example.bot.repository.UserContextRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для управления контекстом пользователя
 */
@Service
public class ContextService {

    /**
     * Репозиторий контекста пользователя
     */
    private final UserContextRepository userContextRepository;

    /**
     * Сервис пользователя
     */
    private final UserService userService;

    /**
     * Конструктор для сервиса контекста пользователя
     * @param userContextRepository Репозиторий контекста пользователя
     * @param userService Сервис пользователя
     */
    public ContextService(UserContextRepository userContextRepository,
                          UserService userService) {
        this.userContextRepository = userContextRepository;
        this.userService = userService;
    }

    /**
     * Получить контекст пользователя
     * @param userId идентификатор пользователя
     * @return контекст пользователя в Optional, пустой Optional если пользователь не найден
     */
    public Optional<UserContext> getContext(Long userId) {
        return userService.getUserById(userId)
                .map(UserEntity::getContext);
    }

    /**
     * Установить текущий тест в контексте пользователя
     * @param userId идентификатор пользователя
     * @param testEntity тест
     */
    public void setCurrentTest(Long userId, TestEntity testEntity) {
        getContext(userId).ifPresent(context -> {
            context.setCurrentTest(testEntity);
            userContextRepository.save(context);
        });
    }

    /**
     * Установить текущий вопрос в контексте
     * @param userId идентификатор пользователя
     * @param question вопрос
     */
    public void setCurrentQuestion(Long userId, QuestionEntity question) {
        getContext(userId).ifPresent(context -> {
            context.setCurrentQuestion(question);
            userContextRepository.save(context);
        });
    }

    /**
     * Получить текущий вопрос из контекста
     * @param userId идентификатор пользователя
     * @return текущий вопрос в Optional
     */
    public Optional<QuestionEntity> getCurrentQuestion(Long userId) {
        return getContext(userId).map(UserContext::getCurrentQuestion);
    }

    /**
     * Получить текущий тест из контекста
     * @param userId идентификатор пользователя
     * @return текущий тест в Optional
     */
    public Optional<TestEntity> getCurrentTest(Long userId) {
        return getContext(userId).map(UserContext::getCurrentTest);
    }

    /**
     * Установить индекс редактируемого ответа
     * @param userId идентификатор пользователя
     * @param editingAnswerIndex индекс редактируемого ответа
     */
    public void setEditingAnswerIndex(Long userId, Integer editingAnswerIndex) {
        getContext(userId).ifPresent(context -> {
            context.setEditingAnswerIndex(editingAnswerIndex);
            userContextRepository.save(context);
        });
    }

    /**
     * Получить индекс редактируемого ответа
     * @param userId идентификатор пользователя
     * @return индекс редактируемого ответа в Optional
     */
    public Optional<Integer> getEditingAnswerIndex(Long userId) {
        return getContext(userId).map(UserContext::getEditingAnswerIndex);
    }

    /**
     * Получить количество правильных ответов в тесте
     * @param userId идентификатор пользователя
     * @return количество правильных ответов в Optional
     */
    public Optional<Integer> getCorrectAnswerCount(Long userId) {
        return getContext(userId).map(UserContext::getCorrectAnswerCount);
    }

    /**
     * Увеличить количество правильно решённых вопросов на 1
     * @param userId идентификатор пользователя
     */
    public void incrementCorrectAnswerCount(Long userId) {
        getContext(userId).ifPresent(context -> {
            context.setCorrectAnswerCount(context.getCorrectAnswerCount() + 1);
            userContextRepository.save(context);
        });
    }

    /**
     * Отчистить счётчик правильно решённых вопросов
     * @param userId идентификатор пользователя
     */
    public void clearCorrectAnswerCount(Long userId) {
        getContext(userId).ifPresent(context -> {
            context.setCorrectAnswerCount(0);
            userContextRepository.save(context);
        });
    }

    /**
     * Получить количество решённых вопросов в тесте
     * @param userId идентификатор пользователя
     * @return количество решённых вопросов в Optional
     */
    public Optional<Integer> getCountAnsweredQuestions(Long userId) {
        return getContext(userId).map(UserContext::getCountAnsweredQuestions);
    }

    /**
     * Увеличить количество решённых вопросов на 1
     * @param userId идентификатор пользователя
     */
    public void incrementCountAnsweredQuestions(Long userId) {
        getContext(userId).ifPresent(context -> {
            context.setCountAnsweredQuestions(context.getCountAnsweredQuestions() + 1);
            userContextRepository.save(context);
        });
    }

    /**
     * Отчистить счётчик правильно решённых вопросов
     * @param userId идентификатор пользователя
     */
    public void clearCountAnsweredQuestions(Long userId) {
        getContext(userId).ifPresent(context -> {
            context.setCountAnsweredQuestions(0);
            userContextRepository.save(context);
        });
    }

    /**
     * Обновление контекста пользователя в базе данных
     * @param context контекст пользователя
     */
    public void updateContext(UserContext context) {
        userContextRepository.save(context);
    }
}