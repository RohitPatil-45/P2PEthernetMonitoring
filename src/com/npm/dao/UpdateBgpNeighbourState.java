/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.EthernetMonitoring;
import com.npm.model.BgpNeighbourState;
import com.npm.model.P2PEthernetModel;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author Kratos
 */
public class UpdateBgpNeighbourState implements Runnable{
    
    String updateQuery = null;

    @Override
    public void run() {
        System.out.println("Start update in bgp_neighbour_state");
        int count = 0;
        updateQuery = "update bgp_neighbour_state set state=?, state_description=?, timestamp=?, bgp_status=?, generate_time=?, clear_time=? where device_ip=?";

        try {
            EthernetMonitoring.updateBgpListTemp.clear();
            EthernetMonitoring.updateBgpListTemp.addAll(EthernetMonitoring.updateBgpList);
            EthernetMonitoring.updateBgpList.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (EthernetMonitoring.updateBgpListTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            for (BgpNeighbourState log : EthernetMonitoring.updateBgpListTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getState());
                    preparedStatement.setString(2, log.getState_description());
                    preparedStatement.setTimestamp(3, log.getTimestamp());
                    preparedStatement.setString(4, log.getBgpStatus());
                    preparedStatement.setTimestamp(5, !log.getBgpStatus().equalsIgnoreCase("Up") ? log.getTimestamp(): null);
                    preparedStatement.setTimestamp(6, log.getBgpStatus().equalsIgnoreCase("Up") ? log.getTimestamp() : null);
                    preparedStatement.setString(7, log.getIp());

                    preparedStatement.addBatch();
             
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
            System.out.println("update " + count + " bgp_neighbour_state records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }
    
}
