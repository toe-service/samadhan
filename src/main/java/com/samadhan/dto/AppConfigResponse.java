package com.samadhan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppConfigResponse {
    private String minVersion;
    private boolean forceUpdate;
}
