package com.github.seecret1.delivery_service;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Базовый класс для интеграционных тестов.
 * 
 * Конфигурирует окружение для запуска интеграционных тестов:
 * - Использует тестовый профиль 'test'
 * - Автоматически конфигурирует встроенную БД для тестов
 * - Загружает контекст Spring приложения
 * - Запускает тесты в транзакциях с автоматическим откатом
 * - Использует PostgreSQL контейнер если доступен Docker, иначе H2
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Tests")
public abstract class SpringBootApplicationTest {
    
}

