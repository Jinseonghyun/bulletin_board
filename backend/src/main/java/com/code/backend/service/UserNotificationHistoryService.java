package com.code.backend.service;

import com.code.backend.entity.Article;
import com.code.backend.entity.Comment;
import com.code.backend.entity.UserNotificationHistory;
import com.code.backend.repository.UserNotificationHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserNotificationHistoryService {

    private UserNotificationHistoryRepository userNotificationHistoryRepository;

    public UserNotificationHistoryService(UserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    public void insertArticleNotification(Article article, Long userId) {
        UserNotificationHistory history = new UserNotificationHistory();
        history.setTitle("글이 작성되었습니다.");
        history.setContent(article.getTitle());
        history.setUserId(userId);
        userNotificationHistoryRepository.save(history);
    }

    public void insertCommentNotification(Comment comment, Long userId) {
        UserNotificationHistory history = new UserNotificationHistory();
        history.setTitle("댓글이 작성되었습니다.");
        history.setContent(comment.getContent());
        history.setUserId(userId);
        userNotificationHistoryRepository.save(history);
    }

    public void readNotification(String id) {
        Optional<UserNotificationHistory> history = userNotificationHistoryRepository.findById(id);
        if (history.isEmpty()) {
            return;
        }
        history.get().setIsRead(true);
        history.get().setUpdatedDate(LocalDateTime.now());
        userNotificationHistoryRepository.save(history.get());
    }
}
