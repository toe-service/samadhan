package com.samadhan.dto;

import com.samadhan.entity.Driver;

import java.util.List;
import java.util.Map;

public class ServiceCentreWrapper {

    private Long id;
    private String driverName;
    private String driverContactNumber;
    private String driverToken;
    private String latitude;
    private String longitude;
    private String destinationLatitude;
    private String destinationLongitude;
    private String sourceLatitude;
    private String sourceLongitude;

    public void setSourceLatitude(String sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public void setSourceLongitude(String sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public String getSourceLatitude() {
        return sourceLatitude;
    }

    public String getSourceLongitude() {
        return sourceLongitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setDestinationLatitude(String destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public void setDestinationLongitude(String destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDestinationLatitude() {
        return destinationLatitude;
    }

    public String getDestinationLongitude() {
        return destinationLongitude;
    }

    public Long getId() {
        return id;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverContactNumber() {
        return driverContactNumber;
    }

    public String getDriverToken() {
        return driverToken;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverContactNumber(String driverContactNumber) {
        this.driverContactNumber = driverContactNumber;
    }

    public void setDriverToken(String driverToken) {
        this.driverToken = driverToken;
    }
}
