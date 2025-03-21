package com.code.backend.service;

import com.code.backend.pojo.ArticleNotification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(ArticleNotification articleNotification) {
        rabbitTemplate.convertAndSend("code-notification", articleNotification.toString());
    }
}
