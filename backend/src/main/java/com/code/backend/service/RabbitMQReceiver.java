package com.code.backend.service;

import com.code.backend.entity.Comment;
import com.code.backend.pojo.SendCommentNotification;
import com.code.backend.pojo.WriteComment;
import com.code.backend.repository.CommentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RabbitMQReceiver {

    private CommentRepository commentRepository;
    private RabbitMQSender rabbitMQSender;

    public RabbitMQReceiver(CommentRepository commentRepository, RabbitMQSender rabbitMQSender) {
        this.commentRepository = commentRepository;
        this.rabbitMQSender = rabbitMQSender;
    }

    @RabbitListener(queues = "code-notification")
    public void receive(String message) {
        if (message.contains(WriteComment.class.getSimpleName())) {
            this.sendCommentNotification(message);
            return;
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                System.out.println("Received Message = " + message);
            }
        }, 5000);  // 5 second

    }

    private void sendCommentNotification(String message) {
        message = message.replace("WriteComment(", "").replace(")", "");
        String[] parts = message.split(", ");
        String type = null;
        Long commentId = null;
        for (String part : parts) {
            String[] keyValue = part.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (key.equals("type")) {
                type = value;
            } else if (key.equals("commentId")) {
                commentId = Long.parseLong(value);
            }
        }
        WriteComment writeComment = new WriteComment();
        writeComment.setType(type);
        writeComment.setCommentId(commentId);

        // 알림 전송
        SendCommentNotification sendCommentNotification = new SendCommentNotification();
        sendCommentNotification.setCommentId(writeComment.getCommentId());
        Optional<Comment> comment = commentRepository.findById(writeComment.getCommentId());
        if (comment.isEmpty()) {
            return;
        }
        HashSet<Long> userSet = new HashSet<>();
        // 댓글 작성한 본인
        userSet.add(comment.get().getAuthor().getId());
        // 글 작성자
        userSet.add(comment.get().getArticle().getAuthor().getId());
        // 댓글 작성자 모두
        List<Comment> comments = commentRepository.findByArticleId(comment.get().getArticle().getId());
        for (Comment article_comment : comments) {
            userSet.add(article_comment.getAuthor().getId());
        }
        for (Long userId : userSet) {
            sendCommentNotification.setUserId(userId);
            rabbitMQSender.send(sendCommentNotification);
        }
    }
}