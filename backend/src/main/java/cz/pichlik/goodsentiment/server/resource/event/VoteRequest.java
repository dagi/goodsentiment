/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.resource.event;

public class VoteRequest {
    private Integer sentimentCode;
    private String orgUnit;
    private Double latitude;
    private Double longitude;
    private String city;
    private String gender;
    private Integer yearsInCompany;
    private Long timestamp;

    public Integer getSentimentCode() {
        return sentimentCode;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getCity() {
        return city;
    }

    public String getGender() {
        return gender;
    }

    public Integer getYearsInCompany() {
        return yearsInCompany;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setSentimentCode(Integer sentimentCode) {
        this.sentimentCode = sentimentCode;
    }

    public void setOrgUnit(String orgUnit) {
        this.orgUnit = orgUnit;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setYearsInCompany(Integer yearsInCompany) {
        this.yearsInCompany = yearsInCompany;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "VoteRequest [sentimentCode=" + sentimentCode + ", orgUnit=" + orgUnit + ", latitude=" + latitude + ", longitude="
                + longitude + ", city=" + city + ", gender=" + gender + ", yearsInCompany=" + yearsInCompany + ", timestamp=" + timestamp
                + "]";
    }
}
