/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.EthernetMonitoring;
import com.npm.model.BgpNeighbourState;
import com.npm.model.OspfNeighbourStateModel;
import com.npm.model.P2PEthernetModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 *
 * @author Kratos
 */
public class DatabaseHelper {

    public HashMap<String, P2PEthernetModel> getNodeData() {
        HashMap<String, P2PEthernetModel> mapNodeData = new HashMap();

        String selectQuery = "SELECT DEVICE_IP, DEVICE_NAME, NEIGHBOUR_IP, NEIGHBOUR_INDEX, STATE, COMMUNITY FROM p2p_ethernet_monitoring WHERE MONITORING_PARAM='Yes'";
        try (
                Connection con = Datasource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(selectQuery);) {

            while (rs.next()) {
                P2PEthernetModel model = new P2PEthernetModel();
                model.setDeviceIp(rs.getString("DEVICE_IP"));
                model.setDeviceName(rs.getString("DEVICE_NAME"));
                model.setLinkIp(rs.getString("NEIGHBOUR_IP"));
                model.setState(rs.getString("STATE"));
                model.setNeighbourIndex(rs.getString("NEIGHBOUR_INDEX"));
                model.setCommunity(rs.getString("COMMUNITY"));

                mapNodeData.put(rs.getString("DEVICE_IP"), model);
                EthernetMonitoring.stateStatus.put(rs.getString("DEVICE_IP"), rs.getString("STATE"));
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching P2PEthernet link ip = " + e);
        }
        return mapNodeData;
    }
    
    
    public HashMap<String, String> getBgpState() {
        HashMap<String, String> mapNodeData = new HashMap();

        String selectQuery = "SELECT device_ip, state FROM bgp_neighbour_state";
        try (
                Connection con = Datasource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(selectQuery);) {

            while (rs.next()) {
              
                EthernetMonitoring.bgpState.put(rs.getString("DEVICE_IP"), rs.getString("STATE"));
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching Bgp State = " + e);
        }
        return mapNodeData;
    }

    public void insertIntoEventLog(String deviceIP, String deviceName, String eventMsg, int severity, String serviceName, Timestamp logtime, String netadmin_msg, String isAffected, String problem, String serviceId, String deviceType) {
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        Connection connection = null;
        try {
            connection = Datasource.getConnection();
            preparedStatement1 = connection.prepareStatement("INSERT INTO event_log (device_id, device_name, service_name, event_msg, netadmin_msg, severity,"
                    + " event_timestamp, acknowledgement_status, isAffected, Problem_Clear, Service_ID, Device_Type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            preparedStatement1.setString(1, deviceIP);
            preparedStatement1.setString(2, deviceName);
            preparedStatement1.setString(3, serviceName);
            preparedStatement1.setString(4, eventMsg);
            preparedStatement1.setString(5, netadmin_msg);
            preparedStatement1.setInt(6, severity);
            preparedStatement1.setTimestamp(7, logtime);
            preparedStatement1.setBoolean(8, false);
            preparedStatement1.setString(9, isAffected);
            preparedStatement1.setString(10, problem);
            preparedStatement1.setString(11, serviceId);
            preparedStatement1.setString(12, deviceType);

            preparedStatement1.executeUpdate();

        } catch (Exception e) {
            System.out.println(deviceIP + "inserting in event log Exception:" + e);
        } finally {
            try {
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }

        try {
            if ("Cleared".equalsIgnoreCase(problem)) {

                String updateQuery = "UPDATE event_log\n"
                        + "SET\n"
                        + "    Cleared_event_timestamp = ?,\n"
                        // + "    netadmin_msg = ?,\n"
                        + "netadmin_msg = CONCAT(netadmin_msg, ' => ', ?),\n"
                        + "    isAffected = ?\n"
                        + "WHERE\n"
                        + "    ID = (\n"
                        + "        SELECT id_alias.ID\n"
                        + "        FROM (\n"
                        + "            SELECT ID\n"
                        + "            FROM event_log\n"
                        + "            WHERE service_id = ?\n"
                        + "              AND device_id = ?\n"
                        + "            AND isaffected = '1' ORDER BY ID DESC\n"
                        + "            LIMIT 1\n"
                        + "        ) AS id_alias\n"
                        + "    )\n"
                        + ";";

                connection = Datasource.getConnection();

                preparedStatement2 = connection.prepareStatement(updateQuery);
                preparedStatement2.setTimestamp(1, logtime);

                preparedStatement2.setString(2, netadmin_msg); // To Do
                preparedStatement2.setString(3, "0");
                preparedStatement2.setString(4, serviceId);
                preparedStatement2.setString(5, deviceIP);

                preparedStatement2.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Exception in update event log = " + e);
        } finally {
            try {
                if (preparedStatement2 != null) {
                    preparedStatement2.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }
    }

    public void neighbourStateStatus(String deviceIP, String state_description, String oid_state, Timestamp logtime) {
        try {
            OspfNeighbourStateModel node = new OspfNeighbourStateModel();
            node.setDeviceIP(deviceIP);
            node.setState(oid_state);
            node.setState_description(state_description);
            node.setEventTimestamp(logtime);
            EthernetMonitoring.stateLog.add(node);
        } catch (Exception exp) {
            System.out.println(deviceIP + "Exception in adding neighbourStateStatus=" + exp);
        }
    }

    public void BgpStateStatus(String deviceIP, String deviceName, String stateValue, String stateText, Timestamp logtime, String bgp_status, Long epochTime) {

        try {
            BgpNeighbourState obj = new BgpNeighbourState();
            obj.setIp(deviceIP);
            obj.setDeviceName(deviceName);
            obj.setState(stateValue);
            obj.setState_description(stateText);
            obj.setTimestamp(logtime);
            obj.setBgpStatus(bgp_status);
            obj.setTimestamp_epoch(epochTime);
            EthernetMonitoring.updateBgpList.add(obj);
            EthernetMonitoring.updateBgpLogList.add(obj);
        } catch (Exception exp) {
            System.out.println(deviceIP + "Exception in adding BgpStateStatus=" + exp);
        }
    }

}
