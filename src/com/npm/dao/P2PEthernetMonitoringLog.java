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
import java.sql.PreparedStatement;

/**
 *
 * @author Kratos
 */
public class P2PEthernetMonitoringLog implements Runnable {

    String insertQuery = null;

    @Override
    public void run() {
        System.out.println("Start update in P2P_Ethernet_Monitoring_Log");
        int count = 0;
        insertQuery = "insert into p2p_ethernet_monitoring_log (DEVICE_IP, DEVICE_NAME, NEIGHBOUR_IP, STATE, EVENT_TIMESTAMP, STATE_DESCRIPTION, timestamp_epoch, STATUS) "
                + "VALUES (?,?,?,?,?,?,?,?)";

        try {
            EthernetMonitoring.updateListlogTemp.clear();
            EthernetMonitoring.updateListlogTemp.addAll(EthernetMonitoring.updatelogList);
            EthernetMonitoring.updatelogList.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (EthernetMonitoring.updateListlogTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            connection.setAutoCommit(false);

            for (P2PEthernetModel log : EthernetMonitoring.updateListlogTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getDeviceIp());
                    preparedStatement.setString(2, log.getDeviceName());
                    preparedStatement.setString(3, log.getLinkIp());
                    preparedStatement.setString(4, log.getState());
                    preparedStatement.setTimestamp(5, log.getEventTimestamp());
                    preparedStatement.setString(6, log.getStateDescription());
                    preparedStatement.setLong(7, log.getTimestamp_epoch());
                    preparedStatement.setString(8, log.getState().equalsIgnoreCase("8") ? "Up" : "Down");
                    preparedStatement.addBatch();
//                    } else {
//                        System.out.println("inside insert");
//                        insertStmt.setString(1, trapLog.getDeviceIP());
//                        insertStmt.setString(2, trapLog.getDeviceName());
//                        insertStmt.setString(3, trapLog.getServiceName());
//                        insertStmt.setString(4, trapLog.getSeverity());
//                        insertStmt.setString(5, trapLog.getAlarmStatus());
//                        insertStmt.setString(6, trapLog.getTrapValue());
//                        insertStmt.setString(7, trapLog.getNodeUptime());
//                        insertStmt.addBatch();
//                    }
                    if (++count % 1 == 0) {
                        System.out.println("inside update batch");
                        preparedStatement.executeBatch();
//                        insertStmt.executeBatch();
                        preparedStatement.clearBatch();
//                        insertStmt.clearBatch();

                    }

                } catch (Exception e) {
                    System.out.println("update error: " + e);
                }
            }

            preparedStatement.executeBatch();
//            insertStmt.executeBatch();
            connection.commit();
            System.out.println("update " + count + " p2p_ethernet_monitoring_log records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }

}
