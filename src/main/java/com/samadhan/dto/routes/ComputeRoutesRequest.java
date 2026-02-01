package com.samadhan.dto.routes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputeRoutesRequest {
    private Waypoint origin;
    private Waypoint destination;
    private String travelMode;
    private String routingPreference;

    private Boolean computeAlternativeRoutes;

    private List<Waypoint> intermediates;
}
