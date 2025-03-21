package com.code.backend.service;

import com.code.backend.pojo.WriteArticle;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(WriteArticle writeArticle) {
        rabbitTemplate.convertAndSend("code-notification", writeArticle.toString());
    }
}
