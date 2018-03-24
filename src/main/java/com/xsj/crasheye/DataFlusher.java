package com.xsj.crasheye;

import android.util.Base64;
import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.pushstrategy.DateRefreshStrategy;
import com.xsj.crasheye.pushstrategy.MergerSession;
import com.xsj.crasheye.util.Utils;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class DataFlusher extends BaseExecutor implements InterfaceExecutor {
    private static final int MAX_FILE_SIZE = 5242880;

    DataFlusher() {
    }

    public synchronized void send() {
        Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
            public void run() {
                DataFlusher.this.BuildNativateErrorData();
                DataFlusher.this.BuildNotSaveNativateErrorData();
                MergerSession.MergerSessionFiles();
                if (!Utils.allowedToSendData()) {
                    Logger.logInfo("You have enabled the FlushOnlyOverWiFi option and there is no WiFi connection, data will not be sent now.");
                } else if (Properties.FILES_PATH != null) {
                    File[] files = new File(Properties.FILES_PATH).listFiles(CrasheyeFileFilter.getInstance());
                    if (files != null && files.length > 0) {
                        boolean isSendReport;
                        if (files.length >= 2) {
                            isSendReport = true;
                        } else {
                            isSendReport = DateRefreshStrategy.getInstance().checkCanReportBySpanTime();
                        }
                        for (File file : files) {
                            if (file.exists()) {
                                if (file.length() > 5242880 || file.length() == 0) {
                                    Utils.deleteFile(file);
                                } else {
                                    if (!isSendReport && file.getName().startsWith(CrasheyeFileFilter.SESIONFIX)) {
                                        if (DateRefreshStrategy.getInstance().checkCanReportByFileCount(MergerSession.GetSessionCountByFileName(file.getName()))) {
                                            isSendReport = true;
                                        }
                                    }
                                    NetSenderResponse nsr = new NetSenderResponse("", null);
                                    try {
                                        String jsonData = Utils.readFile(file.getAbsolutePath());
                                        if (jsonData == null || jsonData.length() == 0) {
                                            if (Crasheye.crasheyeCallback != null) {
                                                Crasheye.crasheyeCallback.netSenderResponse(nsr);
                                            }
                                        } else if (new NetSender().sendBlocking(null, jsonData, false).getSentSuccessfully().booleanValue()) {
                                            if (file.getName().startsWith(CrasheyeFileFilter.SESIONFIX)) {
                                                DateRefreshStrategy.getInstance().updataLastReportTime(Utils.getTimeForLong());
                                                DateRefreshStrategy.getInstance().saveLastReportTime(Properties.AppContent);
                                            }
                                            Utils.deleteFile(file);
                                        }
                                    } catch (Exception e) {
                                        nsr.setException(e);
                                        nsr.setSentSuccessfully(Boolean.valueOf(false));
                                        e.printStackTrace();
                                        if (Crasheye.crasheyeCallback != null) {
                                            Crasheye.crasheyeCallback.netSenderResponse(nsr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        if (getExecutor() != null) {
            getExecutor().execute(t);
        }
    }

    private void BuildNativateErrorData() {
        File[] files = new File(Properties.FILES_PATH).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(CrasheyeFileFilter.NATIVEPREFIX) && pathname.getName().endsWith(CrasheyeFileFilter.POSTFIX)) {
                    return true;
                }
                return false;
            }
        });
        if (files != null) {
            for (File f : files) {
                try {
                    JSONObject json = new JSONObject(Utils.readFile(f.getAbsolutePath()));
                    JSONObject crashNode = json.getJSONObject("crash");
                    String dumpFile = crashNode.getString("dumpfile");
                    File file = new File(dumpFile);
                    if (file.exists()) {
                        byte[] data = Utils.toByteArray(dumpFile);
                        if (data != null) {
                            crashNode.put("file", Base64.encodeToString(data, 2));
                        }
                        BuildNativeErrorCustomData(dumpFile, json);
                        BuildNativeBreadcrumbData(dumpFile, json);
                        BuildMonoStackData(dumpFile, json);
                        Utils.writeFile(CrasheyeFileFilter.createNewFile(), json.toString() + Properties.getSeparator(EnumActionType.ndkerror));
                        Utils.deleteFile(file);
                        Utils.deleteFile(f);
                        DeleteNativeDataFile(dumpFile);
                    } else {
                        Logger.logWarning("native crash dump file is not exists");
                        Utils.deleteFile(f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logWarning("build ndk error report fail");
                }
            }
        }
    }

    private void DeleteNativeDataFile(String dumppath) {
        String[] fileSuffix = new String[]{CrasheyeFileFilter.CUSTOMFILE, CrasheyeFileFilter.BREADCRUMBSFILE, CrasheyeFileFilter.MONOSTACKFILE};
        for (CharSequence replace : fileSuffix) {
            Utils.deleteFile(dumppath.replace(CrasheyeFileFilter.RAWNATIVEFILE, replace));
        }
    }

    private void BuildNativeBreadcrumbData(String dumppath, JSONObject node) {
        String extrapath = dumppath.replace(CrasheyeFileFilter.RAWNATIVEFILE, CrasheyeFileFilter.BREADCRUMBSFILE);
        File f = new File(extrapath);
        if (f.exists() && !f.isDirectory()) {
            byte[] data = Utils.toByteArray(extrapath);
            if (data != null) {
                List<byte[]> pairs = Utils.byteSplit(CrasheyeFileFilter.NATIVESEPARATOR.getBytes(), data);
                if (pairs.size() % 2 == 0) {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < pairs.size(); i += 2) {
                        try {
                            String key = new String((byte[]) pairs.get(i), HTTP.UTF_8);
                            jsonArray.put(new StringBuilder(String.valueOf(key)).append(":").append(new String((byte[]) pairs.get(i + 1), HTTP.UTF_8)).toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (jsonArray.length() != 0) {
                        try {
                            node.put("breadcrumbs", jsonArray);
                        } catch (JSONException e2) {
                            Logger.logError(e2.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void BuildNativeErrorCustomData(String dumppath, JSONObject node) {
        String extrapath = dumppath.replace(CrasheyeFileFilter.RAWNATIVEFILE, CrasheyeFileFilter.CUSTOMFILE);
        File f = new File(extrapath);
        if (f.exists() && !f.isDirectory()) {
            byte[] data = Utils.toByteArray(extrapath);
            if (data != null) {
                List<byte[]> pairs = Utils.byteSplit(CrasheyeFileFilter.NATIVESEPARATOR.getBytes(), data);
                if (pairs.size() % 2 == 0) {
                    String scriptStack = null;
                    Map<String, String> extraData = new HashMap();
                    for (int i = 0; i < pairs.size(); i += 2) {
                        try {
                            String key = new String((byte[]) pairs.get(i), HTTP.UTF_8);
                            String value = new String((byte[]) pairs.get(i + 1), HTTP.UTF_8);
                            if (key.equals("scriptstack")) {
                                scriptStack = value;
                            } else {
                                extraData.put(key, value);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (scriptStack != null) {
                        JSONObject crashNode = node.optJSONObject("crash");
                        if (crashNode != null) {
                            try {
                                crashNode.put("scriptstack", scriptStack);
                            } catch (JSONException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    if (extraData.size() != 0) {
                        try {
                            node.put("extradata", new JSONObject(extraData));
                        } catch (JSONException e22) {
                            Logger.logError(e22.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void BuildMonoStackData(String dumppath, JSONObject node) {
        String extrapath = dumppath.replace(CrasheyeFileFilter.RAWNATIVEFILE, CrasheyeFileFilter.MONOSTACKFILE);
        File f = new File(extrapath);
        if (f.exists() && !f.isDirectory()) {
            byte[] data = Utils.toByteArray(extrapath);
            if (data != null) {
                String monoStack = null;
                try {
                    monoStack = new String(data, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (monoStack != null) {
                    JSONObject crashNode = node.optJSONObject("crash");
                    if (crashNode != null) {
                        try {
                            crashNode.put("scriptstack", monoStack);
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void BuildNotSaveNativateErrorData() {
        File[] files = new File(Properties.FILES_PATH).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(CrasheyeFileFilter.RAWNATIVEFILE)) {
                    return true;
                }
                return false;
            }
        });
        if (files != null) {
            for (File f : files) {
                try {
                    ActionNativeError error = new ActionNativeError(f.getAbsolutePath());
                    error.SetNativeCrashData(f.getAbsolutePath());
                    error.SetLastUnSave();
                    JSONObject json = error.toJson();
                    BuildNativeErrorCustomData(f.getAbsolutePath(), json);
                    BuildNativeBreadcrumbData(f.getAbsolutePath(), json);
                    BuildMonoStackData(f.getAbsolutePath(), json);
                    Utils.writeFile(CrasheyeFileFilter.createNewFile(), json.toString() + Properties.getSeparator(EnumActionType.ndkerror));
                    Utils.deleteFile(f);
                    DeleteNativeDataFile(f.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logWarning("build ndk error report fail");
                }
            }
        }
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }
}
