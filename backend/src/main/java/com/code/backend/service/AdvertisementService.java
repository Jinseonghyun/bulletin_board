package com.code.backend.service;

import com.code.backend.dto.AdvertisementDto;
import com.code.backend.entity.AdViewHistory;
import com.code.backend.entity.Advertisement;
import com.code.backend.repository.AdViewHistoryRepository;
import com.code.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdvertisementService {

    private static final String REDIS_KEY = "ad:";

    private AdvertisementRepository advertisementRepository;
    private RedisTemplate<String, Object> redisTemplate;
    private AdViewHistoryRepository adViewHistoryRepository;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, RedisTemplate<String, Object> redisTemplate, AdViewHistoryRepository adViewHistoryRepository) {
        this.advertisementRepository = advertisementRepository;
        this.redisTemplate = redisTemplate;
        this.adViewHistoryRepository = adViewHistoryRepository;
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
}
