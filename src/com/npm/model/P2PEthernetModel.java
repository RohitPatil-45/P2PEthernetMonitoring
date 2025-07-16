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
public class P2PEthernetModel {

    private Long id;
    private String deviceIp;
    private String deviceName;
    private String linkIp;
    private String state;
    private Timestamp eventTimestamp;
    private String neighbourIndex;
    private String community;
    private String stateDescription;
    private Long timestamp_epoch;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getLinkIp() {
        return linkIp;
    }

    public void setLinkIp(String linkIp) {
        this.linkIp = linkIp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getNeighbourIndex() {
        return neighbourIndex;
    }

    public void setNeighbourIndex(String neighbourIndex) {
        this.neighbourIndex = neighbourIndex;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    public Long getTimestamp_epoch() {
        return timestamp_epoch;
    }

    public void setTimestamp_epoch(Long timestamp_epoch) {
        this.timestamp_epoch = timestamp_epoch;
    }

   

}
