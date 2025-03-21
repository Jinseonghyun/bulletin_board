package com.code.backend.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class RabbitMQReceiver {

    @RabbitListener(queues = "code-notification")
    public void receive(String message) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                System.out.println("Received Message = " + message);
            }
        }, 5000);  // 5 second

    }
}