package com.stericson.RootTools.execution;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;
import com.stericson.RootTools.exceptions.RootToolsException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Executer {
    protected Process process = null;
    protected Result result = null;

    private static class Worker extends Thread {
        private String[] commands;
        private Executer executer;
        public int exit;
        public List<String> finalResponse;
        private int sleepTime;
        private boolean useRoot;

        private Worker(Executer executer, String[] commands, int sleepTime, Result result, boolean useRoot) {
            this.exit = -911;
            this.commands = commands;
            this.sleepTime = sleepTime;
            this.executer = executer;
            this.executer.result = result;
            this.useRoot = useRoot;
        }

        public void run() {
            InputStreamReader osRes;
            Exception e;
            Throwable th;
            DataOutputStream os = null;
            InputStreamReader osRes2 = null;
            InputStreamReader osErr = null;
            try {
                InputStreamReader osErr2;
                if (this.executer.process == null) {
                    Runtime.getRuntime().gc();
                    if (RootTools.customShell.equals("")) {
                        this.executer.process = Runtime.getRuntime().exec(this.useRoot ? "su" : "sh");
                        RootTools.log(this.useRoot ? "Using Root" : "Using sh");
                    } else {
                        this.executer.process = Runtime.getRuntime().exec(RootTools.customShell);
                        RootTools.log("Using custom shell: " + RootTools.customShell);
                    }
                    if (this.executer.result != null) {
                        this.executer.result.setProcess(this.executer.process);
                    }
                }
                DataOutputStream os2 = new DataOutputStream(this.executer.process.getOutputStream());
                try {
                    osRes = new InputStreamReader(this.executer.process.getInputStream());
                    try {
                        osErr2 = new InputStreamReader(this.executer.process.getErrorStream());
                    } catch (InterruptedException e2) {
                        osRes2 = osRes;
                        os = os2;
                        if (os != null) {
                            try {
                                os.writeBytes("exit \n");
                                os.flush();
                                os.close();
                            } catch (Exception e3) {
                                this.executer.closeShell();
                            }
                        }
                        if (osRes2 != null) {
                            osRes2.close();
                        }
                        if (osErr != null) {
                            osErr.close();
                        }
                        this.executer.closeShell();
                    } catch (Exception e4) {
                        e = e4;
                        osRes2 = osRes;
                        os = os2;
                        try {
                            if (RootTools.debugMode) {
                                e.printStackTrace();
                                RootTools.log("Error: " + e.getMessage());
                            }
                            if (os != null) {
                                try {
                                    os.writeBytes("exit \n");
                                    os.flush();
                                    os.close();
                                } catch (Exception e5) {
                                    this.executer.closeShell();
                                }
                            }
                            if (osRes2 != null) {
                                osRes2.close();
                            }
                            if (osErr != null) {
                                osErr.close();
                            }
                            this.executer.closeShell();
                        } catch (Throwable th2) {
                            th = th2;
                            if (os != null) {
                                try {
                                    os.writeBytes("exit \n");
                                    os.flush();
                                    os.close();
                                } catch (Exception e6) {
                                    this.executer.closeShell();
                                    throw th;
                                }
                            }
                            if (osRes2 != null) {
                                osRes2.close();
                            }
                            if (osErr != null) {
                                osErr.close();
                            }
                            this.executer.closeShell();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        osRes2 = osRes;
                        os = os2;
                        if (os != null) {
                            os.writeBytes("exit \n");
                            os.flush();
                            os.close();
                        }
                        if (osRes2 != null) {
                            osRes2.close();
                        }
                        if (osErr != null) {
                            osErr.close();
                        }
                        this.executer.closeShell();
                        throw th;
                    }
                } catch (InterruptedException e7) {
                    os = os2;
                    if (os != null) {
                        os.writeBytes("exit \n");
                        os.flush();
                        os.close();
                    }
                    if (osRes2 != null) {
                        osRes2.close();
                    }
                    if (osErr != null) {
                        osErr.close();
                    }
                    this.executer.closeShell();
                } catch (Exception e8) {
                    e = e8;
                    os = os2;
                    if (RootTools.debugMode) {
                        e.printStackTrace();
                        RootTools.log("Error: " + e.getMessage());
                    }
                    if (os != null) {
                        os.writeBytes("exit \n");
                        os.flush();
                        os.close();
                    }
                    if (osRes2 != null) {
                        osRes2.close();
                    }
                    if (osErr != null) {
                        osErr.close();
                    }
                    this.executer.closeShell();
                } catch (Throwable th4) {
                    th = th4;
                    os = os2;
                    if (os != null) {
                        os.writeBytes("exit \n");
                        os.flush();
                        os.close();
                    }
                    if (osRes2 != null) {
                        osRes2.close();
                    }
                    if (osErr != null) {
                        osErr.close();
                    }
                    this.executer.closeShell();
                    throw th;
                }
                try {
                    BufferedReader bufferedReader = new BufferedReader(osRes);
                    bufferedReader = new BufferedReader(osErr2);
                    List<String> response = null;
                    if (this.executer.result == null) {
                        response = new LinkedList();
                    }
                    try {
                        for (String single : this.commands) {
                            RootTools.log("Shell command: " + single);
                            os2.writeBytes(single + "\n");
                            os2.flush();
                            Thread.sleep((long) this.sleepTime);
                        }
                        os2.writeBytes("exit \n");
                        os2.flush();
                        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                            if (this.executer.result == null) {
                                response.add(line);
                            } else {
                                this.executer.result.process(line);
                            }
                            RootTools.log("input stream: " + line);
                        }
                        RootTools.log("Done reading input stream");
                        for (String line_error = bufferedReader.readLine(); line_error != null; line_error = bufferedReader.readLine()) {
                            if (this.executer.result == null) {
                                response.add(line_error);
                            } else {
                                this.executer.result.processError(line_error);
                            }
                            RootTools.log("error stream: " + line_error);
                        }
                        RootTools.log("Done reading error stream");
                        RootTools.log("In finally block");
                        if (this.executer.process != null) {
                            RootTools.log("Getting Exit");
                            this.finalResponse = response;
                            this.exit = -1;
                            this.exit = this.executer.process.waitFor();
                            RootTools.log("Exit done...");
                            RootTools.lastExitCode = this.exit;
                            if (this.executer.result != null) {
                                this.executer.result.onComplete(this.exit);
                            } else {
                                response.add(Integer.toString(this.exit));
                            }
                        }
                    } catch (Exception ex) {
                        if (RootTools.debugMode) {
                            RootTools.log("Error: " + ex.getMessage());
                        }
                        if (this.executer.result != null) {
                            this.executer.result.onFailure(ex);
                        }
                        RootTools.log("In finally block");
                        if (this.executer.process != null) {
                            RootTools.log("Getting Exit");
                            this.finalResponse = response;
                            this.exit = -1;
                            this.exit = this.executer.process.waitFor();
                            RootTools.log("Exit done...");
                            RootTools.lastExitCode = this.exit;
                            if (this.executer.result != null) {
                                this.executer.result.onComplete(this.exit);
                            } else {
                                response.add(Integer.toString(this.exit));
                            }
                        }
                    } catch (Throwable th5) {
                        RootTools.log("In finally block");
                        if (this.executer.process != null) {
                            RootTools.log("Getting Exit");
                            this.finalResponse = response;
                            this.exit = -1;
                            this.exit = this.executer.process.waitFor();
                            RootTools.log("Exit done...");
                            RootTools.lastExitCode = this.exit;
                            if (this.executer.result != null) {
                                this.executer.result.onComplete(this.exit);
                            } else {
                                response.add(Integer.toString(this.exit));
                            }
                        }
                    }
                    if (os2 != null) {
                        try {
                            os2.writeBytes("exit \n");
                            os2.flush();
                            os2.close();
                        } catch (Exception e9) {
                            osRes2 = osRes;
                            os = os2;
                            osErr = osErr2;
                            this.executer.closeShell();
                        }
                    }
                    if (osRes != null) {
                        try {
                            osRes.close();
                        } catch (Exception e10) {
                            osRes2 = osRes;
                            osErr = osErr2;
                            this.executer.closeShell();
                        }
                    }
                    if (osErr2 != null) {
                        try {
                            osErr2.close();
                        } catch (Exception e11) {
                            osErr = osErr2;
                            this.executer.closeShell();
                        }
                    }
                    osErr = osErr2;
                    this.executer.closeShell();
                } catch (InterruptedException e12) {
                    osErr = osErr2;
                    osRes2 = osRes;
                    os = os2;
                    if (os != null) {
                        os.writeBytes("exit \n");
                        os.flush();
                        os.close();
                    }
                    if (osRes2 != null) {
                        osRes2.close();
                    }
                    if (osErr != null) {
                        osErr.close();
                    }
                    this.executer.closeShell();
                } catch (Exception e13) {
                    e = e13;
                    osErr = osErr2;
                    osRes2 = osRes;
                    os = os2;
                    if (RootTools.debugMode) {
                        e.printStackTrace();
                        RootTools.log("Error: " + e.getMessage());
                    }
                    if (os != null) {
                        os.writeBytes("exit \n");
                        os.flush();
                        os.close();
                    }
                    if (osRes2 != null) {
                        osRes2.close();
                    }
                    if (osErr != null) {
                        osErr.close();
                    }
                    this.executer.closeShell();
                } catch (Throwable th6) {
                    th = th6;
                    osErr = osErr2;
                    osRes2 = osRes;
                    os = os2;
                    if (os != null) {
                        os.writeBytes("exit \n");
                        os.flush();
                        os.close();
                    }
                    if (osRes2 != null) {
                        osRes2.close();
                    }
                    if (osErr != null) {
                        osErr.close();
                    }
                    this.executer.closeShell();
                    throw th;
                }
            } catch (InterruptedException e14) {
                if (os != null) {
                    os.writeBytes("exit \n");
                    os.flush();
                    os.close();
                }
                if (osRes2 != null) {
                    osRes2.close();
                }
                if (osErr != null) {
                    osErr.close();
                }
                this.executer.closeShell();
            } catch (Exception e15) {
                e = e15;
                if (RootTools.debugMode) {
                    e.printStackTrace();
                    RootTools.log("Error: " + e.getMessage());
                }
                if (os != null) {
                    os.writeBytes("exit \n");
                    os.flush();
                    os.close();
                }
                if (osRes2 != null) {
                    osRes2.close();
                }
                if (osErr != null) {
                    osErr.close();
                }
                this.executer.closeShell();
            }
        }
    }

    public synchronized List<String> sendShell(String[] commands, int sleepTime, Result result, boolean useRoot, int timeout) throws IOException, RootToolsException, TimeoutException {
        Worker worker;
        RootTools.log("Sending " + commands.length + " shell command" + (commands.length > 1 ? "s" : ""));
        worker = new Worker(commands, sleepTime, result, useRoot);
        worker.start();
        if (timeout == -1) {
            timeout = 300000;
        }
        try {
            worker.join((long) timeout);
            Thread.sleep((long) RootTools.shellDelay);
            if (worker.exit == -911) {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
        return worker.finalResponse;
    }

    private void closeShell() {
        if (this.process != null) {
            try {
                this.process.destroy();
            } catch (Exception e) {
            }
            this.process = null;
        }
        if (this.result != null) {
            this.result = null;
        }
    }
}
