package com.samadhan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class RouteResponse {
    private long distanceMeters;
    private long durationSeconds;
    private String polyline;

    private List<Leg> legs;

    private List<RouteSummary> alternatives;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteSummary {
        private long distanceMeters;
        private long durationSeconds;
        private String polyline;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Leg {
        private long distanceMeters;
        private long durationSeconds;
        private List<Step> steps;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Step {
        private String instruction;
        private String maneuver;
        private long distanceMeters;
        private long durationSeconds;
        private String polyline;
    }
}
