package com.code.backend.repository;

import com.code.backend.entity.UserNotificationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserNotificationHistoryRepository extends MongoRepository<UserNotificationHistory, String> {
}
