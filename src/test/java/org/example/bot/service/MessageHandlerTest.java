package org.example.bot.service;

import org.example.bot.handler.MessageHandler;
import org.example.bot.processor.MessageProcessor;
import org.example.bot.telegram.BotResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Тесты на обработчик сообщений
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class MessageHandlerTest {

    /**
     * Мок объект обработчика 1
     */
    @Mock
    private MessageProcessor processor1;

    /**
     * Мок объект обработчика 2
     */
    @Mock
    private MessageProcessor processor2;

    /**
     * Обработчик сообщений
     */
    private MessageHandler messageHandler;

    /**
     * Идентификатор пользователя
     */
    private final Long userId = 123L;

    /**
     * Тестовое сообщение
     */
    private final String message = "test message";

    /**
     * Перед каждым тестом инициализировать по новой обработчик сообщений
     */
    @BeforeEach
    void setUp() {
        messageHandler = new MessageHandler(Arrays.asList(processor1, processor2));
    }

    /**
     * Проверить, что вызывается обработчик, который может обработать сообщение,
     * и следующие обработчики даже не проверяются
     */
    @Test
    void shouldReturnProcessorResponseWhenCanProcess() {
        when(processor1.canProcess(userId, message)).thenReturn(true);
        when(processor1.process(userId, message)).thenReturn(new BotResponse("processed message"));


        String actualResponse = messageHandler.handle(message, userId).getMessage();

        Assertions.assertEquals("processed message", actualResponse);
        verify(processor1).canProcess(userId, message);
        verify(processor1).process(userId, message);
        verify(processor2, never()).canProcess(any(), any());
        verify(processor2, never()).process(any(), any());
    }

    /**
     *  Проверить, что вернется сообщение по умолчанию,
     *  когда никакой обработчик не может обработать сообщение
     */
    @Test
    void shouldReturnDefaultMessageWhenNoProcessorCanHandle() {
        when(processor1.canProcess(userId, message)).thenReturn(false);
        when(processor2.canProcess(userId, message)).thenReturn(false);

        String actualResponse = messageHandler.handle(message, userId).getMessage();

        Assertions.assertEquals("Я вас не понимаю, для справки используйте /help", actualResponse);

        verify(processor1).canProcess(userId, message);
        verify(processor2).canProcess(userId, message);
        verify(processor1, never()).process(any(), any());
        verify(processor2, never()).process(any(), any());
    }

    /**
     * Проверить, что вернется сообщение по умолчанию,
     * когда список обработчиков пуст
     */
    @Test
    void shouldHandleEmptyProcessorsList() {
        messageHandler = new MessageHandler(Collections.emptyList());
        String actualResponse = messageHandler.handle(message, userId).getMessage();
        Assertions.assertEquals("Я вас не понимаю, для справки используйте /help", actualResponse);
    }
}