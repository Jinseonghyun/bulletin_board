package com.code.backend.service;

import com.code.backend.dto.AdvertisementDto;
import com.code.backend.entity.Advertisement;
import com.code.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdvertisementService {

    private AdvertisementRepository advertisementRepository;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
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
        return advertisement;
    }

    public List<Advertisement> getAdList() {
        return advertisementRepository.findAll();
    }

    public Optional<Advertisement> getAd(Long adId) {
        return advertisementRepository.findById(adId);
    }
}
