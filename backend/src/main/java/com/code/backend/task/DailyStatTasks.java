package com.code.backend.task;

import com.code.backend.dto.AdHistoryResult;
import com.code.backend.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyStatTasks {

    private final AdvertisementService advertisementService;

    @Autowired
    public DailyStatTasks(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void insertAdViewStatAtMidnight() {
        List<AdHistoryResult> results = advertisementService.getAdViewHistoryGroupedByAdId();
        advertisementService.insertAdViewStat(results);
    }
}
