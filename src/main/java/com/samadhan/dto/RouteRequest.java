package com.samadhan.dto;


import java.util.List;

public record RouteRequest(GeoPoint origin,
                           GeoPoint destination,
                           String travelMode,
                           Boolean alternativeRoutes,
                           List<GeoPoint> waypoints) {
}
