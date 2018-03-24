package org.bitlet.weupnp;

import android.net.http.Headers;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class GatewayDiscover {
    private static final String[] DEFAULT_SEARCH_TYPES = new String[]{"urn:schemas-upnp-org:device:InternetGatewayDevice:1", "urn:schemas-upnp-org:service:WANIPConnection:1", "urn:schemas-upnp-org:service:WANPPPConnection:1"};
    private static final int DEFAULT_TIMEOUT = 3000;
    public static final String IP = "239.255.255.250";
    public static final int PORT = 1900;
    private final Map<InetAddress, GatewayDevice> devices;
    private String[] searchTypes;
    private int timeout;

    private class SendDiscoveryThread extends Thread {
        InetAddress ip;
        String searchMessage;

        SendDiscoveryThread(InetAddress localIP, String searchMessage) {
            this.ip = localIP;
            this.searchMessage = searchMessage;
        }

        public void run() {
            Throwable th;
            DatagramSocket ssdp = null;
            try {
                DatagramSocket ssdp2 = new DatagramSocket(new InetSocketAddress(this.ip, 0));
                try {
                    byte[] searchMessageBytes = this.searchMessage.getBytes();
                    DatagramPacket ssdpDiscoverPacket = new DatagramPacket(searchMessageBytes, searchMessageBytes.length);
                    ssdpDiscoverPacket.setAddress(InetAddress.getByName(GatewayDiscover.IP));
                    ssdpDiscoverPacket.setPort(GatewayDiscover.PORT);
                    ssdp2.send(ssdpDiscoverPacket);
                    ssdp2.setSoTimeout(GatewayDiscover.this.timeout);
                    boolean waitingPacket = true;
                    while (waitingPacket) {
                        DatagramPacket receivePacket = new DatagramPacket(new byte[1536], 1536);
                        try {
                            ssdp2.receive(receivePacket);
                            byte[] receivedData = new byte[receivePacket.getLength()];
                            System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());
                            GatewayDevice gatewayDevice = GatewayDiscover.this.parseMSearchReply(receivedData);
                            gatewayDevice.setLocalAddress(this.ip);
                            gatewayDevice.loadDescription();
                            if (Arrays.asList(GatewayDiscover.this.searchTypes).contains(gatewayDevice.getSt())) {
                                synchronized (GatewayDiscover.this.devices) {
                                    GatewayDiscover.this.devices.put(this.ip, gatewayDevice);
                                    break;
                                }
                            }
                            continue;
                        } catch (SocketTimeoutException e) {
                            waitingPacket = false;
                        }
                    }
                    if (ssdp2 != null) {
                        ssdp2.close();
                        ssdp = ssdp2;
                        return;
                    }
                } catch (Exception e2) {
                    ssdp = ssdp2;
                    if (ssdp != null) {
                        ssdp.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    ssdp = ssdp2;
                    if (ssdp != null) {
                        ssdp.close();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                if (ssdp != null) {
                    ssdp.close();
                }
            } catch (Throwable th3) {
                th = th3;
                if (ssdp != null) {
                    ssdp.close();
                }
                throw th;
            }
        }
    }

    public GatewayDiscover() {
        this(DEFAULT_SEARCH_TYPES);
    }

    public GatewayDiscover(String st) {
        this(new String[]{st});
    }

    public GatewayDiscover(String[] types) {
        this.timeout = DEFAULT_TIMEOUT;
        this.devices = new HashMap();
        this.searchTypes = types;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int milliseconds) {
        this.timeout = milliseconds;
    }

    public Map<InetAddress, GatewayDevice> discover() throws SocketException, UnknownHostException, IOException, SAXException, ParserConfigurationException {
        Collection<InetAddress> ips = getLocalInetAddresses(true, false, false);
        for (String str : this.searchTypes) {
            String searchMessage = "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nST: " + str + "\r\n" + "MAN: \"ssdp:discover\"\r\n" + "MX: 2\r\n" + "\r\n";
            Collection<SendDiscoveryThread> threads = new ArrayList();
            for (InetAddress ip : ips) {
                SendDiscoveryThread thread = new SendDiscoveryThread(ip, searchMessage);
                threads.add(thread);
                thread.start();
            }
            for (SendDiscoveryThread thread2 : threads) {
                try {
                    thread2.join();
                } catch (InterruptedException e) {
                }
            }
            if (!this.devices.isEmpty()) {
                break;
            }
        }
        return this.devices;
    }

    private GatewayDevice parseMSearchReply(byte[] reply) {
        GatewayDevice device = new GatewayDevice();
        StringTokenizer st = new StringTokenizer(new String(reply), "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken().trim();
            if (!(line.isEmpty() || line.startsWith("HTTP/1.") || line.startsWith("NOTIFY *"))) {
                String key = line.substring(0, line.indexOf(58));
                String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;
                key = key.trim();
                if (value != null) {
                    value = value.trim();
                }
                if (key.compareToIgnoreCase(Headers.LOCATION) == 0) {
                    device.setLocation(value);
                } else if (key.compareToIgnoreCase("st") == 0) {
                    device.setSt(value);
                }
            }
        }
        return device;
    }

    public GatewayDevice getValidGateway() {
        for (GatewayDevice device : this.devices.values()) {
            try {
                if (device.isConnected()) {
                    return device;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public Map<InetAddress, GatewayDevice> getAllGateways() {
        return this.devices;
    }

    private List<InetAddress> getLocalInetAddresses(boolean getIPv4, boolean getIPv6, boolean sortIPv4BeforeIPv6) {
        List<InetAddress> arrayIPAddress = new ArrayList();
        int lastIPv4Index = 0;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            if (networkInterfaces != null) {
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface card = (NetworkInterface) networkInterfaces.nextElement();
                    try {
                        if (!(card.isLoopback() || card.isPointToPoint() || card.isVirtual() || !card.isUp())) {
                            Enumeration<InetAddress> addresses = card.getInetAddresses();
                            if (addresses != null) {
                                while (addresses.hasMoreElements()) {
                                    InetAddress inetAddress = (InetAddress) addresses.nextElement();
                                    int index = arrayIPAddress.size();
                                    if (!getIPv4 || !getIPv6) {
                                        if (getIPv4) {
                                            if (!Inet4Address.class.isInstance(inetAddress)) {
                                            }
                                        }
                                        if (getIPv6 && !Inet6Address.class.isInstance(inetAddress)) {
                                        }
                                    } else if (sortIPv4BeforeIPv6 && Inet4Address.class.isInstance(inetAddress)) {
                                        index = lastIPv4Index;
                                        lastIPv4Index++;
                                    }
                                    arrayIPAddress.add(index, inetAddress);
                                }
                            }
                        }
                    } catch (SocketException e) {
                    }
                }
            }
        } catch (SocketException e2) {
        }
        return arrayIPAddress;
    }
}
