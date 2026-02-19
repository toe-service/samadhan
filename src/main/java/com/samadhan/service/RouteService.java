package com.samadhan.service;


import com.samadhan.dto.GeoPoint;
import com.samadhan.dto.RouteRequest;
import com.samadhan.dto.RouteResponse;
import com.samadhan.dto.routes.ComputeRoutesRequest;

import com.samadhan.dto.routes.ComputeRoutesResponse;
import com.samadhan.dto.routes.Waypoint;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {
    private final String mapsApiKey;
    private final RestTemplate restTemplate;
    private final String COMPUTE_ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    RouteService(@Value("${maps.api.key}") String mapsApiKey, RestTemplate restTemplate) {
        this.mapsApiKey = mapsApiKey;
        this.restTemplate = restTemplate;
    }

    public RouteResponse getRoute(RouteRequest req) {

        // 1. prepare request
        ComputeRoutesRequest body = buildRequest(req);

        HttpHeaders headers = getHeadersForRouteAPI();

        // 2. invoke google api
        HttpEntity<ComputeRoutesRequest> entity = new HttpEntity<>(body, headers);
        ResponseEntity<ComputeRoutesResponse> respEntity;
        try {
            respEntity = restTemplate.exchange(COMPUTE_ROUTES_URL, HttpMethod.POST, entity, ComputeRoutesResponse.class);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Routes API call failed: " + ex.getMessage(), ex);
        }
        ComputeRoutesResponse resp = respEntity.getBody();

        // 3. parse the response
        return parsePrimaryRoute(resp);
    }

    @NotNull
    private HttpHeaders getHeadersForRouteAPI() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", mapsApiKey);
        headers.set("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline," +
                "routes.legs.steps.navigationInstruction," +
                "routes.legs.steps.polyline.encodedPolyline," +
                "routes.legs.steps.distanceMeters," +
                "routes.legs.steps.staticDuration," +
                "routes.legs.distanceMeters," +
                "routes.legs.duration"
        );
        return headers;
    }

    private ComputeRoutesRequest buildRequest(RouteRequest req) {
        Waypoint.LatLng originLatLng = new Waypoint.LatLng(req.origin().lat(), req.origin().lng());
        Waypoint origin = new Waypoint(new Waypoint.Location(originLatLng));

        Waypoint.LatLng destLatLng = new Waypoint.LatLng(req.destination().lat(), req.destination().lng());
        Waypoint destination = new Waypoint(new Waypoint.Location(destLatLng));

        String mode = req.travelMode() != null ? req.travelMode() : "DRIVE";
        Boolean alt = req.alternativeRoutes();
        String routingPreference = "DRIVE".equalsIgnoreCase(mode) ? "TRAFFIC_AWARE" : null;

        // add waypoints
        List<Waypoint> intermediates = new ArrayList<>();
        if (req.waypoints() != null && !req.waypoints().isEmpty()) {
            for (GeoPoint gp : req.waypoints()) {
                Waypoint.LatLng latLng = new Waypoint.LatLng(gp.lat(), gp.lng());
                Waypoint.Location loc = new Waypoint.Location(latLng);
                intermediates.add(new Waypoint(loc));
            }
        }

        return new ComputeRoutesRequest(origin, destination, mode, routingPreference, alt, intermediates);
    }

    private RouteResponse parsePrimaryRoute(ComputeRoutesResponse resp) {
        ComputeRoutesResponse.Route primary = resp.getRoutes().get(0);
        long distance = Optional.ofNullable(primary.getDistanceMeters()).orElse(0L);
        long durationSeconds = parseDurationToSeconds(primary.getDuration());
        String polyline = (primary.getPolyline() != null) ? primary.getPolyline().getEncodedPolyline() : "";

        RouteResponse out = RouteResponse.builder()
                .distanceMeters(distance)
                .durationSeconds(durationSeconds)
                .polyline(polyline)
                .build();

        //  parse legs & steps
        if (primary.getLegs() != null && !primary.getLegs().isEmpty()) {
            List<RouteResponse.Leg> legs = new ArrayList<>();

            for (ComputeRoutesResponse.Leg leg : primary.getLegs()) {
                long legDistance = Optional.ofNullable(leg.getDistanceMeters()).orElse(0L);
                long legDuration = parseDurationToSeconds(leg.getDuration());

                List<RouteResponse.Step> steps = new ArrayList<>();
                for (ComputeRoutesResponse.Step s : leg.getSteps()) {
                    String instr = s.getNavigationInstruction() != null ?
                            s.getNavigationInstruction().getInstructions() : "";
                    String maneuver = s.getNavigationInstruction() != null ?
                            s.getNavigationInstruction().getManeuver() : "";

                    long stepDist = Optional.ofNullable(s.getDistanceMeters()).orElse(0L);
                    long stepDur = parseDurationToSeconds(s.getStaticDuration());
                    String stepPoly = s.getPolyline() != null ? s.getPolyline().getEncodedPolyline() : "";

                    steps.add(new RouteResponse.Step(instr, maneuver, stepDist, stepDur, stepPoly));
                }
                legs.add(new RouteResponse.Leg(legDistance, legDuration, steps));
            }
            out.setLegs(legs);
        }

        //  process alternative routes
        List<RouteResponse.RouteSummary> altList = new ArrayList<>();
        for (int i = 1; i < resp.getRoutes().size(); i++) {
            ComputeRoutesResponse.Route alt = resp.getRoutes().get(i);
            altList.add(new RouteResponse.RouteSummary(
                    alt.getDistanceMeters(),
                    parseDurationToSeconds(alt.getDuration()),
                    alt.getPolyline().getEncodedPolyline()
            ));
        }
        out.setAlternatives(altList);

        return out;
    }

    /**
     * short duration parser:
     * 1) If null -> 0
     * 2) Try ISO-8601 (PTnHnMnS) with Duration.parse
     * 3) If ends with 's', parse the numeric prefix
     * 4) Fallback: extract first number
     */
    private static long parseDurationToSeconds(String text) {
        if (text == null || text.isBlank()) return 0L;

        // Try ISO-8601 first (PT...), else simple "Ns" format, else fallback
        try {
            if (text.startsWith("PT")) {
                return java.time.Duration.parse(text).getSeconds();
            }
        } catch (Exception ignored) {}

        if (text.endsWith("s")) {
            try {
                return Long.parseLong(text.substring(0, text.length() - 1));
            } catch (NumberFormatException ignored) {}
        }

        // fallback - extract first number
        String digits = text.replaceAll("[^0-9]", " ").trim();
        if (digits.isEmpty()) return 0L;
        try {
            return Long.parseLong(digits.split("\\s+")[0]);
        } catch (Exception e) {
            return 0L;
        }
    }
}
