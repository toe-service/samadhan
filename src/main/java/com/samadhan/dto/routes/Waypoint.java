package com.samadhan.dto.routes;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Waypoint {
    private Location location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private LatLng latLng;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatLng {
        private double latitude;
        private double longitude;
    }
}
