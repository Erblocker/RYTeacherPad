package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

public abstract class ConfigurationSecurities {
    public abstract String getDisplaySecirityString(ScanResult scanResult);

    public abstract String getScanResultSecurity(ScanResult scanResult);

    public abstract String getWifiConfigurationSecurity(WifiConfiguration wifiConfiguration);

    public abstract boolean isOpenNetwork(String str);

    public abstract void setupSecurity(WifiConfiguration wifiConfiguration, String str, String str2);

    public static ConfigurationSecurities newInstance() {
        if (Version.SDK < 8) {
            return new ConfigurationSecuritiesOld();
        }
        return new ConfigurationSecuritiesV8();
    }
}
