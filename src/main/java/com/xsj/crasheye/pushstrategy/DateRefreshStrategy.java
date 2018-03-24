package com.xsj.crasheye.pushstrategy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.xsj.crasheye.Properties.RemoteSettingsProps;
import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.util.Utils;
import java.text.SimpleDateFormat;

public class DateRefreshStrategy {
    private static final long MILLIS_IN_HOUR = 3600000;
    private static long lastReportTime = -1;
    private static DateRefreshStrategy m_instance;
    private static long recordStartDate = -1;

    public static DateRefreshStrategy getInstance() {
        if (m_instance == null) {
            synchronized (DateRefreshStrategy.class) {
                if (m_instance == null) {
                    m_instance = new DateRefreshStrategy();
                }
            }
        }
        return m_instance;
    }

    public void load(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences("DATAREFRESHSTRATEGY", 0);
        if (preferences != null) {
            try {
                recordStartDate = Long.valueOf(preferences.getLong("recordStartDate", -1)).longValue();
                lastReportTime = Long.valueOf(preferences.getLong("lastReportTime", -1)).longValue();
            } catch (Exception e) {
                Logger.logWarning("load DateRefreshStrategy error.");
            }
        }
    }

    public void saveAll(Context ctx) {
        saveRecordStartDate(ctx);
        saveLastReportTime(ctx);
    }

    public void updataRecordStartDate(long StartDate) {
        if (StartDate <= 0) {
            Logger.logWarning("StartDate time is illegal.");
        }
        recordStartDate = StartDate;
    }

    public void saveRecordStartDate(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences("DATAREFRESHSTRATEGY", 0);
        if (preferences != null) {
            Editor editor = preferences.edit();
            if (editor != null) {
                try {
                    if (recordStartDate >= 0) {
                        editor.putLong("recordStartDate", recordStartDate);
                        if (editor.commit()) {
                            Logger.logInfo("saveRecordStartDate commit success.");
                        } else {
                            Logger.logWarning("saveRecordStartDate commit error.");
                        }
                    }
                } catch (Exception e) {
                    Logger.logWarning("saveRecordStartDate save error.");
                }
            }
        }
    }

    public void updataLastReportTime(long ReportDate) {
        if (ReportDate <= 0) {
            Logger.logWarning("ReportDate time is illegal.");
        }
        lastReportTime = ReportDate;
    }

    public void saveLastReportTime(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences("DATAREFRESHSTRATEGY", 0);
        if (preferences != null) {
            Editor editor = preferences.edit();
            if (editor != null) {
                try {
                    if (recordStartDate >= 0) {
                        editor.putLong("lastReportTime", lastReportTime);
                        if (editor.commit()) {
                            Logger.logInfo("saveLastReportTime commit success.");
                        } else {
                            Logger.logWarning("saveLastReportTime commit error.");
                        }
                    }
                } catch (Exception e) {
                    Logger.logWarning("saveLastReportTime save error.");
                }
            }
        }
    }

    public boolean checkCanRefresh() {
        if (recordStartDate <= 0) {
        }
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            if (!sf.format(Long.valueOf(recordStartDate)).equals(sf.format(Long.valueOf(Utils.getTimeForLong())))) {
                return true;
            }
            Logger.logInfo("today has sent a request to the server, no need to send");
            return false;
        } catch (Exception e) {
            Logger.logWarning("DateRefreshStrategy check Exception.");
            return true;
        }
    }

    public boolean checkCanReportBySpanTime() {
        int actionSpan = RemoteSettingsProps.actionSpan.intValue();
        if (actionSpan == -1) {
            return true;
        }
        if (actionSpan <= 0 || actionSpan >= 24) {
            Logger.logWarning("actionSpan Don't take effect");
            return false;
        } else if (lastReportTime <= 0) {
            Logger.logWarning("lastReportTime is not exist, so can report");
            return true;
        } else if (Utils.getTimeForLong() - lastReportTime < 0) {
            Logger.logWarning("lastReportTime Don't take effect, so can report");
            return true;
        } else if (Utils.getTimeForLong() - lastReportTime > ((long) actionSpan) * MILLIS_IN_HOUR) {
            return true;
        } else {
            Logger.logInfo("set session report send span time " + actionSpan + " hour.");
            return false;
        }
    }

    public boolean checkCanReportByFileCount(int fileCount) {
        if (fileCount > 0 && fileCount >= RemoteSettingsProps.actionCounts.intValue()) {
            return true;
        }
        return false;
    }
}
