package com.farproc.wifi.connecter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

public class ConfigurationSecuritiesV8 extends ConfigurationSecurities {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$farproc$wifi$connecter$ConfigurationSecuritiesV8$PskType = null;
    static final int SECURITY_EAP = 3;
    static final int SECURITY_NONE = 0;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_WEP = 1;
    private static final String TAG = "ConfigurationSecuritiesV14";

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$farproc$wifi$connecter$ConfigurationSecuritiesV8$PskType() {
        int[] iArr = $SWITCH_TABLE$com$farproc$wifi$connecter$ConfigurationSecuritiesV8$PskType;
        if (iArr == null) {
            iArr = new int[PskType.values().length];
            try {
                iArr[PskType.UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[PskType.WPA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[PskType.WPA2.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[PskType.WPA_WPA2.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$com$farproc$wifi$connecter$ConfigurationSecuritiesV8$PskType = iArr;
        }
        return iArr;
    }

    private static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (config.wepKeys[0] == null) {
            return 0;
        }
        return 1;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains(ConfigurationSecuritiesOld.WEP)) {
            return 1;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    public String getWifiConfigurationSecurity(WifiConfiguration wifiConfig) {
        return String.valueOf(getSecurity(wifiConfig));
    }

    public String getScanResultSecurity(ScanResult scanResult) {
        return String.valueOf(getSecurity(scanResult));
    }

    public void setupSecurity(WifiConfiguration config, String security, String password) {
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        int sec = security == null ? 0 : Integer.valueOf(security).intValue();
        int passwordLen = password == null ? 0 : password.length();
        switch (sec) {
            case 0:
                config.allowedKeyManagement.set(0);
                return;
            case 1:
                config.allowedKeyManagement.set(0);
                config.allowedAuthAlgorithms.set(0);
                config.allowedAuthAlgorithms.set(1);
                if (passwordLen == 0) {
                    return;
                }
                if ((passwordLen == 10 || passwordLen == 26 || passwordLen == 58) && password.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = password;
                    return;
                } else {
                    config.wepKeys[0] = new StringBuilder(String.valueOf('\"')).append(password).append('\"').toString();
                    return;
                }
            case 2:
                config.allowedKeyManagement.set(1);
                if (passwordLen == 0) {
                    return;
                }
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                    return;
                } else {
                    config.preSharedKey = new StringBuilder(String.valueOf('\"')).append(password).append('\"').toString();
                    return;
                }
            case 3:
                config.allowedKeyManagement.set(2);
                config.allowedKeyManagement.set(3);
                return;
            default:
                Log.e(TAG, "Invalid security type: " + sec);
                return;
        }
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        }
        if (wpa2) {
            return PskType.WPA2;
        }
        if (wpa) {
            return PskType.WPA;
        }
        Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
        return PskType.UNKNOWN;
    }

    public String getDisplaySecirityString(ScanResult scanResult) {
        int security = getSecurity(scanResult);
        if (security == 2) {
            switch ($SWITCH_TABLE$com$farproc$wifi$connecter$ConfigurationSecuritiesV8$PskType()[getPskType(scanResult).ordinal()]) {
                case 2:
                    return ConfigurationSecuritiesOld.WPA;
                case 3:
                case 4:
                    return ConfigurationSecuritiesOld.WPA2;
                default:
                    return "?";
            }
        }
        switch (security) {
            case 0:
                return "OPEN";
            case 1:
                return ConfigurationSecuritiesOld.WEP;
            case 3:
                return "EAP";
            default:
                return "?";
        }
    }

    public boolean isOpenNetwork(String security) {
        return String.valueOf(0).equals(security);
    }
}
