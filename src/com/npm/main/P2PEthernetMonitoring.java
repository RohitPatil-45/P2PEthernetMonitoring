/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.npm.dao.P2PStateHistory;
import com.npm.dao.P2PEthernetMonitoringLog;
import com.npm.dao.UpdateP2PEthernetMonitoring;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Kratos
 */
public class P2PEthernetMonitoring {

    public static boolean isSimulation = true;
    public static void main(String[] args) {
        try {
            Thread t2 = new Thread(new EthernetMonitoring());
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception EthernetMonitoring:" + e);
        }
        
        
        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
            //Insertion in SNMPTrapLog Table
            scheduler.scheduleAtFixedRate(new UpdateP2PEthernetMonitoring(), 0, 10, TimeUnit.SECONDS);

            //update in snmp_trap_live_status;
            scheduler.scheduleAtFixedRate(new P2PEthernetMonitoringLog(), 0, 10, TimeUnit.SECONDS);
            
            //insert into p2p state history
            scheduler.scheduleAtFixedRate(new P2PStateHistory(), 0, 10, TimeUnit.SECONDS);
            
        } catch (Exception e) {
             System.out.println("Exception === "+e);
        }
    }
    
}
