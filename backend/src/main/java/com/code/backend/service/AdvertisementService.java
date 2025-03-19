package com.code.backend.service;

import com.code.backend.dto.AdHistoryResult;
import com.code.backend.dto.AdvertisementDto;
import com.code.backend.entity.AdClickHistory;
import com.code.backend.entity.AdViewHistory;
import com.code.backend.entity.Advertisement;
import com.code.backend.repository.AdClickHistoryRepository;
import com.code.backend.repository.AdViewHistoryRepository;
import com.code.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdvertisementService {

    private static final String REDIS_KEY = "ad:";

    private AdvertisementRepository advertisementRepository;
    private RedisTemplate<String, Object> redisTemplate;
    private AdViewHistoryRepository adViewHistoryRepository;
    private AdClickHistoryRepository adClickHistoryRepository;
    private MongoTemplate mongoTemplate;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, RedisTemplate<String, Object> redisTemplate, AdViewHistoryRepository adViewHistoryRepository, AdClickHistoryRepository adClickHistoryRepository, MongoTemplate mongoTemplate) {
        this.advertisementRepository = advertisementRepository;
        this.redisTemplate = redisTemplate;
        this.adViewHistoryRepository = adViewHistoryRepository;
        this.adClickHistoryRepository = adClickHistoryRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public Advertisement writeAd(AdvertisementDto advertisementDto) {
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDto.getTitle());
        advertisement.setContent(advertisementDto.getContent());
        advertisement.setIsDeleted(advertisementDto.getIsDeleted());
        advertisement.setIsVisible(advertisementDto.getIsVisible());
        advertisement.setStartDate(advertisementDto.getStartDate());
        advertisement.setEndDate(advertisementDto.getEndDate());
        advertisement.setViewCount(advertisementDto.getViewCount());
        advertisement.setClickCount(advertisementDto.getClickCount());
        advertisementRepository.save(advertisement);
        redisTemplate.opsForHash().put(REDIS_KEY + advertisement.getId(), advertisement.getId(), advertisement);
        return advertisement;
    }

    public List<Advertisement> getAdList() {
        return advertisementRepository.findAll();
    }

    public Optional<Advertisement> getAd(Long adId, String clientIp, Boolean isTrueView) {
        this.insertAdViewHistory(adId, clientIp, isTrueView);

        Object tempObj = redisTemplate.opsForHash().get(REDIS_KEY, adId);
        if (tempObj != null) {
            return Optional.ofNullable((Advertisement) redisTemplate.opsForHash().get(REDIS_KEY, adId));
        }
        return advertisementRepository.findById(adId);
    }

    public void clickAd(Long adId, String clientIp) {
        AdClickHistory adClickHistory = new AdClickHistory();
        adClickHistory.setAdId(adId);
        adClickHistory.setClientIp(clientIp);
        adClickHistory.setCreatedDate(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!principal.equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) principal;
            adClickHistory.setUsername(userDetails.getUsername());
        }
        adClickHistoryRepository.save(adClickHistory);
    }

    private void insertAdViewHistory(Long adId, String clientIp, Boolean isTrueView) {
        AdViewHistory adViewHistory = new AdViewHistory();
        adViewHistory.setAdId(adId);
        adViewHistory.setClientIp(clientIp);
        adViewHistory.setIsTrueView(isTrueView);
        adViewHistory.setCreatedDate(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!principal.equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) principal;
            adViewHistory.setUsername(userDetails.getUsername());
        }
        adViewHistoryRepository.save(adViewHistory);
    }

    public List<AdHistoryResult> getAdViewHistoryGroupedByAdId() {
        List<AdHistoryResult> usernameResult = this.getAdViewHistoryGroupedByAdIdAndUsername();
        List<AdHistoryResult> clientIpResult = this.getAdViewHistoryGroupedByAdIdAndClientIp();
        HashMap<Long, Integer> totalResult = new HashMap<>();
        for (AdHistoryResult item : usernameResult) {
            totalResult.put(item.getAdId(), item.getCount());
        }
        for (AdHistoryResult item : clientIpResult) {
            totalResult.merge(item.getAdId(), item.getCount(), Integer::sum);
        }

        List<AdHistoryResult> resultList = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : totalResult.entrySet()) {
            AdHistoryResult result = new AdHistoryResult();
            result.setAdId(entry.getKey());
            result.setCount(entry.getValue());
            resultList.add(result);
        }
        return resultList;
    }

    private List<AdHistoryResult> getAdViewHistoryGroupedByAdIdAndUsername() {
        // 어제의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

        // Match 단계: 어제 날짜에 해당하고, username이 있는 문서 필터링
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("createdDate").gte(startOfDay).lt(endOfDay)
                        .and("username").exists(true)
        );

        // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성
        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet("username").as("uniqueUsernames");

        // Projection 단계: 고유한 username 집합의 크기를 count로 계산
        ProjectionOperation projectStage = Aggregation.project()
                .andExpression("_id").as("adId")
                .andExpression("size(uniqueUsernames)").as("count");

        // Aggregation 수행
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
        AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adClickHistory", AdHistoryResult.class);

        return results.getMappedResults();
    }

    private List<AdHistoryResult> getAdViewHistoryGroupedByAdIdAndClientIp() {
        // 어제의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

        // Match 단계: 어제 날짜에 해당하고, username이 있는 문서 필터링
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("createdDate").gte(startOfDay).lt(endOfDay)
                        .and("username").exists(false)
        );

        // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성
        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet("clientIp").as("uniqueClientIp");

        // Projection 단계: 고유한 username 집합의 크기를 count로 계산
        ProjectionOperation projectStage = Aggregation.project()
                .andExpression("_id").as("adId")
                .andExpression("size(uniqueClientIp)").as("count");

        // Aggregation 수행
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
        AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adViewHistory", AdHistoryResult.class);

        return results.getMappedResults();
    }
}
