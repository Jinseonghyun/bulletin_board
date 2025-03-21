package com.code.backend.service;

import com.code.backend.pojo.SendCommentNotification;
import com.code.backend.pojo.WriteArticle;
import com.code.backend.pojo.WriteComment;
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

    public void send(WriteComment message) {
        rabbitTemplate.convertAndSend("code-notification", message.toString());
    }

    public void send(SendCommentNotification message) {
        rabbitTemplate.convertAndSend("send_notification_exchange", "", message.toString());
    }
}
