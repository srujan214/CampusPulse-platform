package com.campuspulse.dto;

public class QrLocationDto {
    private String name;
    private String building;
    private String floor;
    private String targetUrl;
    private String qrBase64;

    public QrLocationDto() {
    }

    public QrLocationDto(String name, String building, String floor, String targetUrl, String qrBase64) {
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.targetUrl = targetUrl;
        this.qrBase64 = qrBase64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getQrBase64() {
        return qrBase64;
    }

    public void setQrBase64(String qrBase64) {
        this.qrBase64 = qrBase64;
    }
}
