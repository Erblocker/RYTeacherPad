package com.stericson.RootTools.execution;

import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.stericson.RootTools.RootTools;
import java.io.IOException;
import java.io.OutputStreamWriter;

public abstract class Command {
    final String[] command;
    int exitCode;
    boolean finished = false;
    int id = 0;
    int timeout = DeviceOperationRESTServiceProvider.TIMEOUT;

    public abstract void output(int i, String str);

    public Command(int id, String... command) {
        this.command = command;
        this.id = id;
    }

    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.timeout = timeout;
    }

    public String getCommand() {
        StringBuilder sb = new StringBuilder();
        for (String append : this.command) {
            sb.append(append);
            sb.append('\n');
        }
        RootTools.log("Sending command(s): " + sb.toString());
        return sb.toString();
    }

    public void writeCommand(OutputStreamWriter out) throws IOException {
        out.write(getCommand());
    }

    public void commandFinished(int id) {
        RootTools.log("Command " + id + "finished.");
    }

    public void setExitCode(int code) {
        synchronized (this) {
            this.exitCode = code;
            this.finished = true;
            commandFinished(this.id);
            notifyAll();
        }
    }

    public void terminate(String reason) {
        try {
            Shell.closeAll();
            RootTools.log("Terminating all shells.");
            terminated(reason);
        } catch (IOException e) {
        }
    }

    public void terminated(String reason) {
        setExitCode(-1);
        RootTools.log("Command " + this.id + " did not finish.");
    }

    public void waitForFinish(int timeout) throws InterruptedException {
        synchronized (this) {
            while (!this.finished) {
                wait((long) timeout);
                if (!this.finished) {
                    this.finished = true;
                    RootTools.log("Timeout Exception has occurred.");
                    terminate("Timeout Exception");
                }
            }
        }
    }

    public int exitCode(int timeout) throws InterruptedException {
        synchronized (this) {
            waitForFinish(timeout);
        }
        return this.exitCode;
    }

    public void waitForFinish() throws InterruptedException {
        synchronized (this) {
            waitForFinish(this.timeout);
        }
    }

    public int exitCode() throws InterruptedException {
        int exitCode;
        synchronized (this) {
            exitCode = exitCode(this.timeout);
        }
        return exitCode;
    }
}
