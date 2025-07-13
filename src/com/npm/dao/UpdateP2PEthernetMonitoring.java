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
public class UpdateP2PEthernetMonitoring implements Runnable{

   String updateQuery = null;

    @Override
    public void run() {
        System.out.println("Start update in P2P Ethernet Monitoring");
        int count = 0;
        updateQuery = "update p2p_ethernet_monitoring set STATE=?,EVENT_TIMESTAMP=? where DEVICE_IP=? and LINK_IP=?";      

        try {
            EthernetMonitoring.updateListTemp.clear();
            EthernetMonitoring.updateListTemp.addAll(EthernetMonitoring.updateList);
            EthernetMonitoring.updateList.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (EthernetMonitoring.updateListTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            for (P2PEthernetModel log : EthernetMonitoring.updateListTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getState());
                    preparedStatement.setTimestamp(2, log.getEventTimestamp());
                    preparedStatement.setString(3, log.getDeviceIp());
                    preparedStatement.setString(4, log.getLinkIp());
               
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
                    e.printStackTrace();
                    System.out.println("update error: " + e);
                }
            }

            preparedStatement.executeBatch();
//            insertStmt.executeBatch();
            connection.commit();
            System.out.println("update " + count + " P2P Ethernet Monitoring records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }
    
}
