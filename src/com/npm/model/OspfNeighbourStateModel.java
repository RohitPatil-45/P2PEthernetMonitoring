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
public class OspfNeighbourStateModel {
    
    private String deviceIP;
    private String state;
    private String state_description;
    private Timestamp eventTimestamp;

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
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

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    
    
    
}
