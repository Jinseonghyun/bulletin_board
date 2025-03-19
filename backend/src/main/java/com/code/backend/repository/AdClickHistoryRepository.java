package com.code.backend.repository;

import com.code.backend.entity.AdClickHistory;
import com.code.backend.entity.AdViewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdClickHistoryRepository extends MongoRepository<AdClickHistory, Long> {
}
