package com.code.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Device {
    private String deviceName;
    private String token; // FCM 에서 발급 해주는 TOKEN
}
