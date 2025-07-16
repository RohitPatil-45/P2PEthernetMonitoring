/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.EthernetMonitoring;
import com.npm.model.OspfNeighbourStateModel;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author Kratos
 */
public class P2PStateHistory implements Runnable {

    String insertQuery = null;

    @Override
    public void run() {
        System.out.println("Start insert in P2PStateHistory");
        int count = 0;
        insertQuery = "insert into p2p_state_history (DEVICE_IP, STATE, STATE_DESCRIPTION, EVENT_TIMESTAMP) VALUES (?,?,?,?)";

        try {
            EthernetMonitoring.stateLogTemp.clear();
            EthernetMonitoring.stateLogTemp.addAll(EthernetMonitoring.stateLog);
            EthernetMonitoring.stateLog.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (EthernetMonitoring.stateLogTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            connection.setAutoCommit(false);

            for (OspfNeighbourStateModel log : EthernetMonitoring.stateLogTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getDeviceIP());
                    preparedStatement.setString(2, log.getState());
                    preparedStatement.setString(3, log.getState_description());

                    preparedStatement.setTimestamp(5, log.getEventTimestamp());

                    preparedStatement.addBatch();
                   
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
            System.out.println("update " + count + " P2PStateHistory records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }

}
