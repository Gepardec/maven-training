package com.gepardec.maventraining;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageServiceTest {

    private static MessageService messageService;

    @BeforeAll
    static void setUp() {
        messageService = new MessageService();
    }

    @Test
    void getApplicationTitleTest() {
        assertNotNull(messageService.getApplicationTitle());
    }

    @Test
    void getApplicationDescriptionTest() {
        assertNotNull(messageService.getApplicationDescription());
    }
}