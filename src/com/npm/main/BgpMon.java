/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npm.dao.DatabaseHelper;
import com.npm.model.EventLog;
import com.npm.model.P2PEthernetModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;

/**
 *
 * @author Kratos
 */
public class BgpMon implements Runnable {

    P2PEthernetModel p2pObj = null;
    DatabaseHelper db = new DatabaseHelper();

    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;

    private String address;
    private String username;
    private String password;

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private final ObjectMapper mapper = new ObjectMapper();

    public BgpMon(P2PEthernetModel model) {
        this.p2pObj = model;
    }

    @Override
    public void run() {
        String deviceIP = p2pObj.getDeviceIp();

        String isAffected = "";
        String problem = "";
        String serviceId = "bgp_state";
        String eventMsg = null;
        String netadmin_msg = null;
        int severity;
        String state_description = "";
        String stateText = "";
        String stateValue = "";
        String bgp_status = "";
        String oldBgpState = null;
        Timestamp logtime = new Timestamp(System.currentTimeMillis());
        long epochTime = System.currentTimeMillis() / 1000;

        try {
            telnet.connect(address, 23);
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());

            readUntil("Username:");
            write(username);

            readUntil("Password:");
            write(password);

            String result = sendCommand("show ip bgp neighbors | include BGP state");

            Pattern pattern = Pattern.compile("BGP state = (\\w+)");
            Matcher matcher = pattern.matcher(result);

            if (matcher.find()) {
                stateText = matcher.group(1);
                stateValue = "Established".equalsIgnoreCase(stateText) ? "1" : "0";
                bgp_status = "Established".equalsIgnoreCase(stateText) ? "Up" : "Down";

                db.BgpStateStatus(deviceIP, p2pObj.getDeviceName(), stateValue, stateText, logtime, bgp_status, epochTime);

                oldBgpState = EthernetMonitoring.bgpState.get(deviceIP).toString();
                if (!oldBgpState.equalsIgnoreCase(stateValue)) {

                    eventMsg = "BGP State value = " + stateValue;
                    netadmin_msg = eventMsg;
                    isAffected = "Established".equalsIgnoreCase(stateText) ? "1" : "0";;
                    problem = "Established".equalsIgnoreCase(stateText) ? "Cleared" : "problem";
                    sendEventLogToApi(deviceIP, p2pObj.getDeviceName(), eventMsg, 5, "BGP_State", logtime, netadmin_msg, isAffected, problem, serviceId, "SWITCH", 0);

                }

                System.out.println("BGP State Text: " + stateText);
                System.out.println("BGP State Value: " + stateValue);
            } else {
                System.out.println("BGP State not found in response.");
            }

            sendCommand("quit");
            telnet.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void write(String value) {
        out.println(value);
        out.flush();
    }

    private String readUntil(String pattern) throws Exception {
        char lastChar = pattern.charAt(pattern.length() - 1);
        StringBuilder sb = new StringBuilder();
        char ch;
        while ((ch = (char) in.read()) != -1) {
            sb.append(ch);
            if (ch == lastChar && sb.toString().endsWith(pattern)) {
                break;
            }
        }
        return sb.toString();
    }

    private String sendCommand(String command) throws Exception {
        write(command);
        Thread.sleep(1000); // wait for output
        StringBuilder sb = new StringBuilder();
        while (in.available() > 0) {
            sb.append((char) in.read());
        }
        return sb.toString();
    }

    public void sendEventLogToApi(String deviceID, String deviceName, String eventMsg, int severity, String serviceName, Timestamp evenTimestamp,
            String netadmin_msg, String isAffected, String problem, String serviceId, String deviceType, int attempt) {
        EventLog log = new EventLog();
        log.setDeviceId(deviceID);
        log.setDeviceName(deviceName);
        log.setEventMsg(eventMsg);
        log.setSeverity(String.valueOf(severity));
        log.setServiceName(serviceName);
        log.setEventTimestamp(evenTimestamp);
        log.setNetadminMsg(netadmin_msg);
        log.setIsaffected(Integer.valueOf(isAffected));
        log.setProblemClear(problem);
        log.setServiceID(serviceId);
        log.setDeviceType(deviceType);

        System.out.println("service id = " + serviceId);
        System.out.println("sAffected = " + isAffected);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String json = mapper.writeValueAsString(log);
            HttpPost request = new HttpPost("http://localhost:8083/api/event/log"); // adjust host/port
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(json));

            CloseableHttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("Log sent successfully: " + statusCode);
            } else {
                System.err.println("Failed to send log, status: " + statusCode);
                retryIfNeeded(log, attempt);
            }

            response.close();
        } catch (IOException e) {
            System.err.println("Exception while sending log: " + e.getMessage());
            retryIfNeeded(log, attempt);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void retryIfNeeded(EventLog log, int attempt) {
        if (attempt < MAX_RETRIES) {
            System.out.println("Retrying sendEventLogToApi... Attempt " + (attempt + 1));
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                return;
            }

            // Retry the API call with incremented attempt count
            sendEventLogToApi(
                    log.getDeviceId(),
                    log.getDeviceName(),
                    log.getEventMsg(),
                    Integer.valueOf(log.getSeverity()),
                    log.getServiceName(),
                    log.getEventTimestamp(),
                    log.getNetadminMsg(),
                    log.getIsaffected().toString(),
                    log.getProblemClear(),
                    log.getServiceID(),
                    log.getDeviceType(),
                    attempt + 1
            );
        } else {
            System.err.println("Max retries reached. Dropping event log.");
        }
    }

}
