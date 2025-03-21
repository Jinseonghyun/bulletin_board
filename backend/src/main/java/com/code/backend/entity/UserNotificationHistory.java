package com.code.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "UserNotificationHistory")
@Getter
@Setter
public class UserNotificationHistory {

    @Id
    private String id;

    private String title;

    private String content;

    private Long userId;

    private Boolean isRead = false;

    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate = LocalDateTime.now();
}
