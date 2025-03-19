package com.code.backend.service;

import com.code.backend.dto.AdvertisementDto;
import com.code.backend.entity.Advertisement;
import com.code.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdvertisementService {

    private static final String REDIS_KEY = "ad:";

    private AdvertisementRepository advertisementRepository;
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, RedisTemplate<String, Object> redisTemplate) {
        this.advertisementRepository = advertisementRepository;
        this.redisTemplate = redisTemplate;
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

    public Optional<Advertisement> getAd(Long adId) {
        Object tempObj = redisTemplate.opsForHash().get(REDIS_KEY, adId);
        if (tempObj != null) {
            return Optional.ofNullable((Advertisement) redisTemplate.opsForHash().get(REDIS_KEY, adId));
        }
        return advertisementRepository.findById(adId);
    }
}
