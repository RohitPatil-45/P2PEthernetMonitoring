/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.EthernetMonitoring;
import com.npm.model.P2PEthernetModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author Kratos
 */
public class DatabaseHelper {

    public HashMap<String, P2PEthernetModel> getNodeData() {
         HashMap<String, P2PEthernetModel> mapNodeData = new HashMap();

        String selectQuery = "SELECT DEVICE_IP, DEVICE_NAME, NEIGHBOUR_IP, NEIGHBOUR_INDEX, STATE, COMMUNITY, STATE_DESCRIPTION FROM p2p_ethernet_monitoring WHERE MONITORING_PARAM='Yes'";
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
                EthernetMonitoring.stateStatus.put(rs.getString("DEVICE_IP"), rs.getString("STATE") + "~" + rs.getString("STATE_DESCRIPTION"));
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching P2PEthernet link ip = " + e);
        }
        return mapNodeData;
    }
    
}
