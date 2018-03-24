package com.xsj.crasheye;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncDataSaver extends BaseDataSaver {
    public synchronized void save(String jsonData) {
        save(jsonData, CrasheyeFileFilter.createNewFile());
    }

    public synchronized void save(final String jsonData, final String fileFullPath) {
        Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
            public void run() {
                Throwable th;
                File file = new File(fileFullPath);
                DataSaverResponse dsr = new DataSaverResponse(jsonData, file.getAbsolutePath());
                if (!(file == null || file.exists())) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        IOException e2;
                        e2.printStackTrace();
                    }
                }
                BufferedWriter bWritter = null;
                try {
                    BufferedWriter bWritter2 = new BufferedWriter(new FileWriter(file, true));
                    try {
                        bWritter2.append(jsonData);
                        bWritter2.flush();
                        bWritter2.close();
                        if (bWritter2 != null) {
                            try {
                                bWritter2.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        dsr.setSavedSuccessfully(Boolean.valueOf(true));
                        if (Crasheye.crasheyeCallback != null) {
                            Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                        }
                        if (null != null) {
                            new DataFlusher().send();
                        }
                        bWritter = bWritter2;
                    } catch (IOException e3) {
                        e22 = e3;
                        bWritter = bWritter2;
                        try {
                            e22.printStackTrace();
                            dsr.setException(e22);
                            dsr.setSavedSuccessfully(Boolean.valueOf(false));
                            if (Crasheye.crasheyeCallback != null) {
                                Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                            }
                            if (bWritter != null) {
                                try {
                                    bWritter.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                            dsr.setSavedSuccessfully(Boolean.valueOf(true));
                            if (Crasheye.crasheyeCallback != null) {
                                Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                            }
                            if (null != null) {
                                new DataFlusher().send();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (bWritter != null) {
                                try {
                                    bWritter.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                            dsr.setSavedSuccessfully(Boolean.valueOf(true));
                            if (Crasheye.crasheyeCallback != null) {
                                Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                            }
                            if (null != null) {
                                new DataFlusher().send();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bWritter = bWritter2;
                        if (bWritter != null) {
                            bWritter.close();
                        }
                        dsr.setSavedSuccessfully(Boolean.valueOf(true));
                        if (Crasheye.crasheyeCallback != null) {
                            Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                        }
                        if (null != null) {
                            new DataFlusher().send();
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e2222 = e4;
                    e2222.printStackTrace();
                    dsr.setException(e2222);
                    dsr.setSavedSuccessfully(Boolean.valueOf(false));
                    if (Crasheye.crasheyeCallback != null) {
                        Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                    }
                    if (bWritter != null) {
                        bWritter.close();
                    }
                    dsr.setSavedSuccessfully(Boolean.valueOf(true));
                    if (Crasheye.crasheyeCallback != null) {
                        Crasheye.crasheyeCallback.dataSaverResponse(dsr);
                    }
                    if (null != null) {
                        new DataFlusher().send();
                    }
                }
            }
        });
        if (getExecutor() != null) {
            getExecutor().execute(t);
        }
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }
}
