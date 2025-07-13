/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.npm.dao.DatabaseHelper;
import com.npm.datasource.Datasource;
import com.npm.model.P2PEthernetModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Kratos
 */
public class EthernetMonitoring implements Runnable {

    public static HashMap<String, P2PEthernetModel> mapNodeData = null;

    public static ArrayList<P2PEthernetModel> updateList = null;
    public static ArrayList<P2PEthernetModel> updateListTemp = null;

    public static ArrayList<P2PEthernetModel> updatelogList = null;
    public static ArrayList<P2PEthernetModel> updateListlogTemp = null;
    
    public static HashMap stateStatus = null;
            

    private static final int THREAD_POOL_SIZE = 8;
    private static final int MONITOR_INTERVAL_SECONDS = 30;

    @Override
    public void run() {
        updateList = new ArrayList<>();
        updateListTemp = new ArrayList<>();

        updatelogList = new ArrayList<>();
        updateListlogTemp = new ArrayList<>();
        
        stateStatus = new HashMap<>();
        
       
        DatabaseHelper helper = new DatabaseHelper();
        mapNodeData = helper.getNodeData();
        System.out.println(mapNodeData.size() + ":EthernetMonitoring:" + mapNodeData);

        Iterator<Map.Entry<String, P2PEthernetModel>> itr = mapNodeData.entrySet().iterator();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

        for (Map.Entry<String, P2PEthernetModel> entry : mapNodeData.entrySet()) {
            P2PEthernetModel model = entry.getValue();

            Runnable task = new P2PEthernetMon(model);

            scheduler.scheduleAtFixedRate(task, 0, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

}
