package com.code.backend.controller;

import com.code.backend.dto.WriteDeviceDto;
import com.code.backend.entity.Device;
import com.code.backend.entity.User;
import com.code.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final UserService userService;


    @Autowired
    public DeviceController(UserService userService) {
        this.userService = userService;

    }

    @GetMapping("")
    public ResponseEntity<List<Device>> getDevices() {
        return ResponseEntity.ok(userService.getDevices());
    }

    @PostMapping("")
    public ResponseEntity<Device> addDevices(@RequestBody WriteDeviceDto writeDeviceDto) {
        return ResponseEntity.ok(userService.addDevice(writeDeviceDto));
    }
}
