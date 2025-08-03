/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.model;

import java.sql.Timestamp;

/**
 *
 * @author Kratos
 */
public class BgpNeighbourState {

    private String bgpStatus;
    private String ip;
    private Timestamp timestamp;
    private String state;
    private String state_description;
    private String deviceName;
    private String isNotification;
    private Long timestamp_epoch;

    public String getBgpStatus() {
        return bgpStatus;
    }

    public void setBgpStatus(String bgpStatus) {
        this.bgpStatus = bgpStatus;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState_description() {
        return state_description;
    }

    public void setState_description(String state_description) {
        this.state_description = state_description;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIsNotification() {
        return isNotification;
    }

    public void setIsNotification(String isNotification) {
        this.isNotification = isNotification;
    }

    public Long getTimestamp_epoch() {
        return timestamp_epoch;
    }

    public void setTimestamp_epoch(Long timestamp_epoch) {
        this.timestamp_epoch = timestamp_epoch;
    }
    
    

}
