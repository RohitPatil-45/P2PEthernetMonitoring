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
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author Kratos
 */
public class BgpNeighbourStateLog implements Runnable {

    String insertQuery = null;

    @Override
    public void run() {
        System.out.println("Start insert in bgp_monitoring_log");
        int count = 0;
        insertQuery = "insert into bgp_monitoring_log (DEVICE_IP, DEVICE_NAME, EVENT_TIMESTAMP, STATE, BGP_STATUS, STATE_DESCRIPTION, timestamp_epoch) VALUES (?,?,?,?,?,?,?)";

        try {
            EthernetMonitoring.updateBgpLogListTemp.clear();
            EthernetMonitoring.updateBgpLogListTemp.addAll(EthernetMonitoring.updateBgpLogList);
            EthernetMonitoring.updateBgpLogList.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (EthernetMonitoring.updateBgpLogListTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            connection.setAutoCommit(false);

            for (BgpNeighbourState log : EthernetMonitoring.updateBgpLogListTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getIp());
                    preparedStatement.setString(2, log.getDeviceName());
                    preparedStatement.setTimestamp(3, log.getTimestamp());
                    preparedStatement.setString(4, log.getState());
                    preparedStatement.setString(5, log.getBgpStatus());
                    preparedStatement.setString(6, log.getState_description());
                    preparedStatement.setLong(7, log.getTimestamp_epoch());

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
            System.out.println("update " + count + " bgp_monitoring_log records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }

}
