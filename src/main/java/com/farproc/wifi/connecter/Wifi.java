package com.farproc.wifi.connecter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Wifi {
    private static final String BSSID_ANY = "any";
    public static final ConfigurationSecurities ConfigSec = ConfigurationSecurities.newInstance();
    private static final int MAX_PRIORITY = 99999;
    private static final String TAG = "Wifi Connecter";

    public static boolean changePasswordAndConnect(Context ctx, WifiManager wifiMgr, WifiConfiguration config, String newPassword, int numOpenNetworksKept) {
        ConfigSec.setupSecurity(config, ConfigSec.getWifiConfigurationSecurity(config), newPassword);
        if (wifiMgr.updateNetwork(config) == -1) {
            return false;
        }
        wifiMgr.disconnect();
        return connectToConfiguredNetwork(ctx, wifiMgr, config, true);
    }

    public static boolean connectToNewNetwork(Context ctx, WifiManager wifiMgr, ScanResult scanResult, String password, int numOpenNetworksKept) {
        String security = ConfigSec.getScanResultSecurity(scanResult);
        if (ConfigSec.isOpenNetwork(security)) {
            checkForExcessOpenNetworkAndSave(wifiMgr, numOpenNetworksKept);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = convertToQuotedString(scanResult.SSID);
        config.BSSID = scanResult.BSSID;
        ConfigSec.setupSecurity(config, security, password);
        int id = -1;
        try {
            id = wifiMgr.addNetwork(config);
        } catch (NullPointerException e) {
            Log.e(TAG, "Weird!! Really!! What's wrong??", e);
        }
        if (id == -1 || !wifiMgr.saveConfiguration()) {
            return false;
        }
        config = getWifiConfiguration(wifiMgr, config, security);
        if (config != null) {
            return connectToConfiguredNetwork(ctx, wifiMgr, config, true);
        }
        return false;
    }

    public static boolean connectToConfiguredNetwork(Context ctx, WifiManager wifiMgr, WifiConfiguration config, boolean reassociate) {
        String security = ConfigSec.getWifiConfigurationSecurity(config);
        int oldPri = config.priority;
        int newPri = getMaxPriority(wifiMgr) + 1;
        if (newPri > MAX_PRIORITY) {
            newPri = shiftPriorityAndSave(wifiMgr);
            config = getWifiConfiguration(wifiMgr, config, security);
            if (config == null) {
                return false;
            }
        }
        config.priority = newPri;
        int networkId = wifiMgr.updateNetwork(config);
        if (networkId == -1) {
            return false;
        }
        if (!wifiMgr.enableNetwork(networkId, false)) {
            config.priority = oldPri;
            return false;
        } else if (wifiMgr.saveConfiguration()) {
            config = getWifiConfiguration(wifiMgr, config, security);
            if (config == null) {
                return false;
            }
            ReenableAllApsWhenNetworkStateChanged.schedule(ctx);
            if (!wifiMgr.enableNetwork(config.networkId, true)) {
                return false;
            }
            if (reassociate ? wifiMgr.reassociate() : wifiMgr.reconnect()) {
                return true;
            }
            return false;
        } else {
            config.priority = oldPri;
            return false;
        }
    }

    private static void sortByPriority(List<WifiConfiguration> configurations) {
        Collections.sort(configurations, new Comparator<WifiConfiguration>() {
            public int compare(WifiConfiguration object1, WifiConfiguration object2) {
                return object1.priority - object2.priority;
            }
        });
    }

    private static boolean checkForExcessOpenNetworkAndSave(WifiManager wifiMgr, int numOpenNetworksKept) {
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        sortByPriority(configurations);
        boolean modified = false;
        int tempCount = 0;
        for (int i = configurations.size() - 1; i >= 0; i--) {
            WifiConfiguration config = (WifiConfiguration) configurations.get(i);
            if (ConfigSec.isOpenNetwork(ConfigSec.getWifiConfigurationSecurity(config))) {
                tempCount++;
                if (tempCount >= numOpenNetworksKept) {
                    modified = true;
                    wifiMgr.removeNetwork(config.networkId);
                }
            }
        }
        if (modified) {
            return wifiMgr.saveConfiguration();
        }
        return true;
    }

    private static int shiftPriorityAndSave(WifiManager wifiMgr) {
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        sortByPriority(configurations);
        int size = configurations.size();
        for (int i = 0; i < size; i++) {
            WifiConfiguration config = (WifiConfiguration) configurations.get(i);
            config.priority = i;
            wifiMgr.updateNetwork(config);
        }
        wifiMgr.saveConfiguration();
        return size;
    }

    private static int getMaxPriority(WifiManager wifiManager) {
        int pri = 0;
        for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

    public static WifiConfiguration getWifiConfiguration(WifiManager wifiMgr, ScanResult hotsopt, String hotspotSecurity) {
        String ssid = convertToQuotedString(hotsopt.SSID);
        if (ssid.length() == 0) {
            return null;
        }
        String bssid = hotsopt.BSSID;
        if (bssid == null) {
            return null;
        }
        if (hotspotSecurity == null) {
            hotspotSecurity = ConfigSec.getScanResultSecurity(hotsopt);
        }
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null) {
            return null;
        }
        for (WifiConfiguration config : configurations) {
            if (config.SSID != null && ssid.equals(config.SSID)) {
                if ((config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid.equals(config.BSSID)) && hotspotSecurity.equals(ConfigSec.getWifiConfigurationSecurity(config))) {
                    return config;
                }
            }
        }
        return null;
    }

    public static WifiConfiguration getWifiConfiguration(WifiManager wifiMgr, WifiConfiguration configToFind, String security) {
        String ssid = configToFind.SSID;
        if (ssid.length() == 0) {
            return null;
        }
        String bssid = configToFind.BSSID;
        if (security == null) {
            security = ConfigSec.getWifiConfigurationSecurity(configToFind);
        }
        for (WifiConfiguration config : wifiMgr.getConfiguredNetworks()) {
            if (config.SSID != null && ssid.equals(config.SSID)) {
                if ((config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid == null || bssid.equals(config.BSSID)) && security.equals(ConfigSec.getWifiConfigurationSecurity(config))) {
                    return config;
                }
            }
        }
        return null;
    }

    public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int lastPos = string.length() - 1;
        return (lastPos > 0 && string.charAt(0) == '\"' && string.charAt(lastPos) == '\"') ? string : "\"" + string + "\"";
    }
}
