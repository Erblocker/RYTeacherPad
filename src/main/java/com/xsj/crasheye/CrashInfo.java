package com.xsj.crasheye;

import com.xsj.crasheye.log.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class CrashInfo extends BaseExecutor implements InterfaceExecutor {
    CrashInfo() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static int getTotalCrashesNum() {
        Throwable th;
        int i = 0;
        Integer crashCounter = Integer.valueOf(i);
        if (Properties.FILES_PATH == null) {
            Logger.logWarning("Please use getTotalCrashesNum after initializing the plugin! Returning 0.");
        } else {
            File file = new File(Properties.FILES_PATH + "/" + "crashCounter");
            if (!(file == null || file.exists())) {
                try {
                    file.createNewFile();
                    i = crashCounter.intValue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            BufferedReader bin = null;
            try {
                BufferedReader bin2 = new BufferedReader(new FileReader(file));
                try {
                    crashCounter = Integer.valueOf(Integer.parseInt(bin2.readLine().trim()));
                } catch (Exception e2) {
                    crashCounter = Integer.valueOf(0);
                } catch (Throwable th2) {
                    th = th2;
                    bin = bin2;
                    if (bin != null) {
                        try {
                            bin.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    throw th;
                }
                i = crashCounter.intValue();
                if (bin2 != null) {
                    try {
                        bin2.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                Exception e5 = e4;
                try {
                    Logger.logWarning("There was a problem getting the crash counter");
                    if (Crasheye.DEBUG) {
                        e5.printStackTrace();
                    }
                    if (bin != null) {
                        try {
                            bin.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    return i;
                } catch (Throwable th3) {
                    th = th3;
                    if (bin != null) {
                        bin.close();
                    }
                    throw th;
                }
            }
        }
        return i;
    }

    protected void clearCrashCounter() {
        Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
            public void run() {
                File file = new File(Properties.FILES_PATH + "/" + "crashCounter");
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
        });
        ExecutorService executor = getExecutor();
        if (t != null && executor != null) {
            executor.submit(t);
        }
    }

    protected void saveCrashCounter() {
        Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
            public void run() {
                IOException e;
                BufferedWriter bos;
                Throwable th;
                File file = new File(Properties.FILES_PATH + "/" + "crashCounter");
                if (!(file == null || file.exists())) {
                    try {
                        file.createNewFile();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                BufferedReader bin = null;
                BufferedWriter bos2 = null;
                try {
                    Integer crashCounter;
                    BufferedReader bin2 = new BufferedReader(new FileReader(file));
                    try {
                        crashCounter = Integer.valueOf(0);
                        try {
                            crashCounter = Integer.valueOf(Integer.parseInt(bin2.readLine().trim()));
                        } catch (Exception e3) {
                            crashCounter = Integer.valueOf(0);
                        }
                        bos = new BufferedWriter(new FileWriter(file));
                    } catch (IOException e4) {
                        e2 = e4;
                        bin = bin2;
                        try {
                            Logger.logWarning("There was a problem saving the crash counter");
                            if (Crasheye.DEBUG) {
                                e2.printStackTrace();
                            }
                            if (bin != null) {
                                try {
                                    bin.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            if (bos2 == null) {
                                try {
                                    bos2.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                    return;
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (bin != null) {
                                try {
                                    bin.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                            if (bos2 != null) {
                                try {
                                    bos2.close();
                                } catch (IOException e22222) {
                                    e22222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bin = bin2;
                        if (bin != null) {
                            bin.close();
                        }
                        if (bos2 != null) {
                            bos2.close();
                        }
                        throw th;
                    }
                    try {
                        bos.write(String.valueOf(crashCounter));
                        bos.newLine();
                        bos.flush();
                        bos.close();
                        if (bin2 != null) {
                            try {
                                bin2.close();
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                        if (bos != null) {
                            try {
                                bos.close();
                            } catch (IOException e2222222) {
                                e2222222.printStackTrace();
                            }
                        }
                        bos2 = bos;
                        bin = bin2;
                    } catch (IOException e5) {
                        e2222222 = e5;
                        bos2 = bos;
                        bin = bin2;
                        Logger.logWarning("There was a problem saving the crash counter");
                        if (Crasheye.DEBUG) {
                            e2222222.printStackTrace();
                        }
                        if (bin != null) {
                            bin.close();
                        }
                        if (bos2 == null) {
                            bos2.close();
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        bos2 = bos;
                        bin = bin2;
                        if (bin != null) {
                            bin.close();
                        }
                        if (bos2 != null) {
                            bos2.close();
                        }
                        throw th;
                    }
                } catch (IOException e6) {
                    e2222222 = e6;
                    Logger.logWarning("There was a problem saving the crash counter");
                    if (Crasheye.DEBUG) {
                        e2222222.printStackTrace();
                    }
                    if (bin != null) {
                        bin.close();
                    }
                    if (bos2 == null) {
                        bos2.close();
                    }
                }
            }
        });
        ExecutorService executor = getExecutor();
        if (t != null && executor != null) {
            executor.submit(t);
        }
    }

    protected void saveLastCrashID(final String lastID) {
        if (lastID != null) {
            Thread t = new LowPriorityThreadFactory().newThread(new Runnable() {
                public void run() {
                    Throwable th;
                    File file = new File(Properties.FILES_PATH + "/" + "lastCrashID");
                    if (!(file == null || file.exists())) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            IOException e2;
                            e2.printStackTrace();
                        }
                    }
                    BufferedWriter bos = null;
                    try {
                        BufferedWriter bos2 = new BufferedWriter(new FileWriter(file));
                        try {
                            bos2.write(lastID);
                            bos2.newLine();
                            bos2.flush();
                            bos2.close();
                            if (bos2 != null) {
                                try {
                                    bos2.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            bos = bos2;
                        } catch (IOException e3) {
                            e22 = e3;
                            bos = bos2;
                            try {
                                Logger.logWarning("There was a problem saving the last crash id");
                                if (Crasheye.DEBUG) {
                                    e22.printStackTrace();
                                }
                                if (bos != null) {
                                    try {
                                        bos.close();
                                    } catch (IOException e222) {
                                        e222.printStackTrace();
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (bos != null) {
                                    try {
                                        bos.close();
                                    } catch (IOException e2222) {
                                        e2222.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            bos = bos2;
                            if (bos != null) {
                                bos.close();
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e2222 = e4;
                        Logger.logWarning("There was a problem saving the last crash id");
                        if (Crasheye.DEBUG) {
                            e2222.printStackTrace();
                        }
                        if (bos != null) {
                            bos.close();
                        }
                    }
                }
            });
            ExecutorService executor = getExecutor();
            if (t != null && executor != null) {
                executor.submit(t);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static String getLastCrashID() {
        Exception e;
        Throwable th;
        File file = new File(Properties.FILES_PATH + "/" + "lastCrashID");
        if (!(file == null || file.exists())) {
            try {
                file.createNewFile();
                return null;
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        BufferedReader bin = null;
        try {
            String crashID;
            BufferedReader bin2 = new BufferedReader(new FileReader(file));
            try {
                crashID = bin2.readLine().trim();
            } catch (Exception e3) {
                e3.printStackTrace();
                crashID = null;
            } catch (Throwable th2) {
                th = th2;
                bin = bin2;
                if (bin != null) {
                    try {
                        bin.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                throw th;
            }
            if (bin2 == null) {
                return crashID;
            }
            try {
                bin2.close();
                return crashID;
            } catch (IOException e222) {
                e222.printStackTrace();
                return crashID;
            }
        } catch (Exception e4) {
            e3 = e4;
            try {
                Logger.logWarning("There was a problem getting the last crash id");
                if (Crasheye.DEBUG) {
                    e3.printStackTrace();
                }
                if (bin != null) {
                    try {
                        bin.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (bin != null) {
                    bin.close();
                }
                throw th;
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
