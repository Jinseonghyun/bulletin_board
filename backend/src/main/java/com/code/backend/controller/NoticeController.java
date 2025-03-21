package com.code.backend.controller;

import com.code.backend.dto.WriteDeviceDto;
import com.code.backend.dto.WriteNoticeDto;
import com.code.backend.entity.Device;
import com.code.backend.entity.Notice;
import com.code.backend.service.NoticeService;
import com.code.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping("")
    public ResponseEntity<Notice> addNotice(@RequestBody WriteNoticeDto dto) {
        return ResponseEntity.ok(noticeService.writeNotice(dto));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<Notice> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }
}
