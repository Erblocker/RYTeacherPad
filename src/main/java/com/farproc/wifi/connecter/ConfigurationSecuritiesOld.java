package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import android.util.Log;
import java.util.BitSet;

public class ConfigurationSecuritiesOld extends ConfigurationSecurities {
    public static final String[] EAP_METHOD = new String[]{"PEAP", "TLS", "TTLS"};
    public static final String IEEE8021X = "IEEE8021X";
    public static final String OPEN = "Open";
    static final String[] SECURITY_MODES = new String[]{WEP, WPA, WPA2, WPA_EAP, IEEE8021X};
    private static final String TAG = "ConfigurationSecuritiesOld";
    public static final String WEP = "WEP";
    public static final int WEP_PASSWORD_ASCII = 1;
    public static final int WEP_PASSWORD_AUTO = 0;
    public static final int WEP_PASSWORD_HEX = 2;
    public static final String WPA = "WPA";
    public static final String WPA2 = "WPA2";
    public static final String WPA_EAP = "WPA-EAP";

    public String getWifiConfigurationSecurity(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(0)) {
            if (wifiConfig.allowedGroupCiphers.get(3) || (!wifiConfig.allowedGroupCiphers.get(0) && !wifiConfig.allowedGroupCiphers.get(1))) {
                return OPEN;
            }
            return WEP;
        } else if (wifiConfig.allowedProtocols.get(1)) {
            return WPA2;
        } else {
            if (wifiConfig.allowedKeyManagement.get(2)) {
                return WPA_EAP;
            }
            if (wifiConfig.allowedKeyManagement.get(3)) {
                return IEEE8021X;
            }
            if (wifiConfig.allowedProtocols.get(0)) {
                return WPA;
            }
            Log.w(TAG, "Unknown security type from WifiConfiguration, falling back on open.");
            return OPEN;
        }
    }

    public String getScanResultSecurity(ScanResult scanResult) {
        String cap = scanResult.capabilities;
        for (int i = SECURITY_MODES.length - 1; i >= 0; i--) {
            if (cap.contains(SECURITY_MODES[i])) {
                return SECURITY_MODES[i];
            }
        }
        return OPEN;
    }

    public String getDisplaySecirityString(ScanResult scanResult) {
        return getScanResultSecurity(scanResult);
    }

    private static boolean isHexWepKey(String wepKey) {
        int len = wepKey.length();
        if (len == 10 || len == 26 || len == 58) {
            return isHex(wepKey);
        }
        return false;
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            char c = key.charAt(i);
            if ((c < '0' || c > '9') && ((c < 'A' || c > 'F') && (c < 'a' || c > 'f'))) {
                return false;
            }
        }
        return true;
    }

    public void setupSecurity(WifiConfiguration config, String security, String password) {
        int i = 1;
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        if (TextUtils.isEmpty(security)) {
            security = OPEN;
            Log.w(TAG, "Empty security, assuming open");
        }
        if (security.equals(WEP)) {
            if (!TextUtils.isEmpty(password)) {
                if (null != null) {
                    String[] strArr = config.wepKeys;
                    if (0 == 1) {
                        password = Wifi.convertToQuotedString(password);
                    }
                    strArr[0] = password;
                } else if (isHexWepKey(password)) {
                    config.wepKeys[0] = password;
                } else {
                    config.wepKeys[0] = Wifi.convertToQuotedString(password);
                }
            }
            config.wepTxKeyIndex = 0;
            config.allowedAuthAlgorithms.set(0);
            config.allowedAuthAlgorithms.set(1);
            config.allowedKeyManagement.set(0);
            config.allowedGroupCiphers.set(0);
            config.allowedGroupCiphers.set(1);
        } else if (security.equals(WPA) || security.equals(WPA2)) {
            config.allowedGroupCiphers.set(2);
            config.allowedGroupCiphers.set(3);
            config.allowedKeyManagement.set(1);
            config.allowedPairwiseCiphers.set(2);
            config.allowedPairwiseCiphers.set(1);
            BitSet bitSet = config.allowedProtocols;
            if (!security.equals(WPA2)) {
                i = 0;
            }
            bitSet.set(i);
            if (!TextUtils.isEmpty(password)) {
                if (password.length() == 64 && isHex(password)) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = Wifi.convertToQuotedString(password);
                }
            }
        } else if (security.equals(OPEN)) {
            config.allowedKeyManagement.set(0);
        } else if (security.equals(WPA_EAP) || security.equals(IEEE8021X)) {
            config.allowedGroupCiphers.set(2);
            config.allowedGroupCiphers.set(3);
            if (security.equals(WPA_EAP)) {
                config.allowedKeyManagement.set(2);
            } else {
                config.allowedKeyManagement.set(3);
            }
            if (!TextUtils.isEmpty(password)) {
                config.preSharedKey = Wifi.convertToQuotedString(password);
            }
        }
    }

    public boolean isOpenNetwork(String security) {
        return OPEN.equals(security);
    }
}
