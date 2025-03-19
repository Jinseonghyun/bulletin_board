package com.code.backend.controller;

import com.code.backend.dto.AdvertisementDto;
import com.code.backend.dto.EditArticleDto;
import com.code.backend.dto.WriteArticleDto;
import com.code.backend.entity.Advertisement;
import com.code.backend.entity.Article;
import com.code.backend.service.AdvertisementService;
import com.code.backend.service.ArticleService;
import com.code.backend.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Autowired
    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping("/admin/ads")
    public ResponseEntity<Advertisement> writeAd(@RequestBody AdvertisementDto advertisementDto) {
        Advertisement advertisement = advertisementService.writeAd(advertisementDto);
        return ResponseEntity.ok(advertisement);
    }

    @GetMapping("/ads")
    public ResponseEntity<List<Advertisement>> getAdList() {
        List<Advertisement> advertisementList = advertisementService.getAdList();
        return ResponseEntity.ok(advertisementList);
    }

    @GetMapping("/ads/{adId}")
    public Object getAdList(@PathVariable Long adId, HttpServletRequest request, @RequestParam(required = false) Boolean isTrueView) {
        String ipAddress = request.getRemoteAddr();
        Optional<Advertisement> advertisementList = advertisementService.getAd(adId, ipAddress, isTrueView != null && isTrueView);
        if (advertisementList.isEmpty()) {
            return ResponseEntity.notFound();
        }
        return ResponseEntity.ok(advertisementList);
    }

}
