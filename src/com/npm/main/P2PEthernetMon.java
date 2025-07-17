/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.npm.dao.DatabaseHelper;
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
    DatabaseHelper db = new DatabaseHelper();

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
        String isAffected = "";
        String problem = "";
        String serviceId = "p2p_state";
        String eventMsg = null;
        String netadmin_msg = null;
        int severity;
        String state_description = "";
        Timestamp logtime = new Timestamp(System.currentTimeMillis());
        long epochTime = System.currentTimeMillis() / 1000;

        try {
            su.start();

            Target target = null;
            
            if (P2PEthernetMonitoring.isSimulation) {
            
                target = su.getTarget("udp:127.0.0.1/161", p2pObj.getCommunity(), SnmpConstants.version2c);

            } else {
                target = su.getTarget("udp:" + deviceIP + "/161", p2pObj.getCommunity(), SnmpConstants.version2c);

            }

            oid_state = su.BandwidthGetVect(target, "Out", new OID(oid_state));
            
            state_description = getState(Integer.valueOf(oid_state));

            P2PEthernetModel obj = new P2PEthernetModel();
            obj.setDeviceIp(deviceIP);
            obj.setDeviceName(p2pObj.getDeviceName());
            obj.setLinkIp(p2pObj.getLinkIp());
            obj.setState(oid_state);
            obj.setStateDescription(state_description);
            obj.setEventTimestamp(logtime);
            obj.setTimestamp_epoch(epochTime);
            EthernetMonitoring.updateList.add(obj);
            EthernetMonitoring.updatelogList.add(obj);

            oldState = EthernetMonitoring.stateStatus.get(deviceIP).toString();
            System.out.println("Old state = "+oldState);
            if (!oldState.equalsIgnoreCase(oid_state)) {
                
                EthernetMonitoring.stateStatus.put(deviceIP, oid_state);

                eventMsg = "P2P Ethernet Monitoring: state = " + oid_state + " - OSPF neighbor state for " + p2pObj.getLinkIp() + " is : " + state_description;
                netadmin_msg = "P2P Ethernet Monitoring: state = " + oid_state + " - OSPF neighbor state for " + p2pObj.getLinkIp() + " is : " + state_description;;
                isAffected = oid_state.equalsIgnoreCase("8") ? "0" : "1";
                problem = oid_state.equalsIgnoreCase("8") ? "Cleared" : "problem";
                severity = oid_state.equalsIgnoreCase("8") ? 0 : 4;
                db.neighbourStateStatus(deviceIP, state_description, oid_state, logtime);
                db.insertIntoEventLog(deviceIP, p2pObj.getDeviceName(), eventMsg, severity, "P2P Ethernet Monitoring", logtime, netadmin_msg, isAffected, problem, serviceId, "SWITCH"); //Evrnt log
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
