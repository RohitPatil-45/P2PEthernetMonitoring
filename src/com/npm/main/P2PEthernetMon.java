/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.npm.model.P2PEthernetModel;
import java.sql.Timestamp;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;

/**
 *
 * @author Kratos
 */
public class P2PEthernetMon implements Runnable {

    P2PEthernetModel p2pObj = null;

    public P2PEthernetMon(P2PEthernetModel model) {
        this.p2pObj = model;
    }

    @Override
    public void run() {
        String deviceIP = p2pObj.getDeviceIp();
        //String oid_state = "1.3.6.1.4.1.2509.8.18.2.10.1.12.1"; // state : value must be between 1 to 8
        String oid_state = "1.3.6.1.2.1.14.10.1.6." + p2pObj.getLinkIp() + "." + p2pObj.getNeighbourIndex();
        SNMPUtil su = new SNMPUtil();
        String oldState = null;
        try {
            su.start();

            Target target = su.getTarget("udp:" + deviceIP + "/161", p2pObj.getCommunity(), SnmpConstants.version2c);

            oid_state = su.BandwidthGetVect(target, "Out", new OID(oid_state));

            P2PEthernetModel obj = new P2PEthernetModel();
            obj.setDeviceIp(deviceIP);
            obj.setDeviceName(p2pObj.getDeviceName());
            obj.setLinkIp(p2pObj.getLinkIp());
            obj.setState(oid_state);
            obj.setStateDescription(getState(Integer.valueOf(oid_state)));
            obj.setEventTimestamp(new Timestamp(System.currentTimeMillis()));
            EthernetMonitoring.updateList.add(obj);

            oldState = EthernetMonitoring.stateStatus.get(deviceIP).toString().split("~")[0];
            if (!oldState.equalsIgnoreCase(p2pObj.getState())) {
                EthernetMonitoring.updatelogList.add(obj);
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching SNMP values: " + e);
        } finally {
            try {
                su.stop();
            } catch (Exception ex2) {
                System.out.println("SNMP Close Exception: " + ex2);
            }
        }

    }

    public String getState(int state) {

        String stateString = null;
        switch (state) {
            case 1:
                stateString = "down";
                break;
            case 2:
                stateString = "attempt";
                break;
            case 3:
                stateString = "init";
                break;
            case 4:
                stateString = "twoWay";
                break;
            case 5:
                stateString = "exchangeStart";
                break;
            case 6:
                stateString = "exchange";
                break;
            case 7:
                stateString = "loading";
                break;
            case 8:
                stateString = "full";
                break;
            default:
                stateString = "unknown";
                break;
        }

        return stateString;

    }

}
