package org.bitlet.weupnp;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class Main {
    private static boolean LIST_ALL_MAPPINGS = false;
    private static int SAMPLE_PORT = 6991;
    private static short WAIT_TIME = (short) 10;

    public static void main(String[] args) throws Exception {
        addLogLine("Starting weupnp");
        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        addLogLine("Looking for Gateway Devices...");
        Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();
        if (gateways.isEmpty()) {
            addLogLine("No gateways found");
            addLogLine("Stopping weupnp");
            return;
        }
        addLogLine(gateways.size() + " gateway(s) found\n");
        int counter = 0;
        for (GatewayDevice gw : gateways.values()) {
            counter++;
            addLogLine("Listing gateway details of device #" + counter + "\n\tFriendly name: " + gw.getFriendlyName() + "\n\tPresentation URL: " + gw.getPresentationURL() + "\n\tModel name: " + gw.getModelName() + "\n\tModel number: " + gw.getModelNumber() + "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress() + "\n");
        }
        GatewayDevice activeGW = gatewayDiscover.getValidGateway();
        if (activeGW != null) {
            addLogLine("Using gateway: " + activeGW.getFriendlyName());
            Integer portMapCount = activeGW.getPortMappingNumberOfEntries();
            addLogLine("GetPortMappingNumberOfEntries: " + (portMapCount != null ? portMapCount.toString() : "(unsupported)"));
            PortMappingEntry portMapping = new PortMappingEntry();
            if (LIST_ALL_MAPPINGS) {
                int pmCount = 0;
                while (activeGW.getGenericPortMappingEntry(pmCount, portMapping)) {
                    addLogLine("Portmapping #" + pmCount + " successfully retrieved (" + portMapping.getPortMappingDescription() + ":" + portMapping.getExternalPort() + ")");
                    pmCount++;
                    if (portMapping == null) {
                        break;
                    }
                }
                addLogLine("Portmapping #" + pmCount + " retrieval failed");
            } else if (activeGW.getGenericPortMappingEntry(0, portMapping)) {
                addLogLine("Portmapping #0 successfully retrieved (" + portMapping.getPortMappingDescription() + ":" + portMapping.getExternalPort() + ")");
            } else {
                addLogLine("Portmapping #0 retrival failed");
            }
            InetAddress localAddress = activeGW.getLocalAddress();
            addLogLine("Using local address: " + localAddress.getHostAddress());
            addLogLine("External address: " + activeGW.getExternalIPAddress());
            addLogLine("Querying device to see if a port mapping already exists for port " + SAMPLE_PORT);
            if (activeGW.getSpecificPortMappingEntry(SAMPLE_PORT, "TCP", portMapping)) {
                addLogLine("Port " + SAMPLE_PORT + " is already mapped. Aborting test.");
                return;
            }
            addLogLine("Mapping free. Sending port mapping request for port " + SAMPLE_PORT);
            if (activeGW.addPortMapping(SAMPLE_PORT, SAMPLE_PORT, localAddress.getHostAddress(), "TCP", "test")) {
                addLogLine("Mapping SUCCESSFUL. Waiting " + WAIT_TIME + " seconds before removing mapping...");
                Thread.sleep((long) (WAIT_TIME * 1000));
                if (activeGW.deletePortMapping(SAMPLE_PORT, "TCP")) {
                    addLogLine("Port mapping removed, test SUCCESSFUL");
                } else {
                    addLogLine("Port mapping removal FAILED");
                }
            }
            addLogLine("Stopping weupnp");
            return;
        }
        addLogLine("No active gateway device found");
        addLogLine("Stopping weupnp");
    }

    private static void addLogLine(String line) {
        System.out.print(DateFormat.getTimeInstance().format(new Date()) + ": " + line + "\n");
    }
}
