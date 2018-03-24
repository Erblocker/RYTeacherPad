package com.stericson.RootTools.execution;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.http.protocol.HTTP;

public class Shell {
    private static Shell customShell = null;
    private static String error = "";
    private static Shell rootShell = null;
    private static Shell shell = null;
    private static int shellTimeout = 10000;
    private static final String token = "F*D^W@#FGF";
    private boolean close = false;
    private final List<Command> commands = new ArrayList();
    private final BufferedReader in;
    private Runnable input = new Runnable() {
        public void run() {
            try {
                Shell.this.writeCommands();
            } catch (IOException e) {
                RootTools.log(e.getMessage(), 2, e);
            }
        }
    };
    private final OutputStreamWriter out;
    private Runnable output = new Runnable() {
        public void run() {
            try {
                Shell.this.readOutput();
            } catch (IOException e) {
                RootTools.log(e.getMessage(), 2, e);
            } catch (InterruptedException e2) {
                RootTools.log(e2.getMessage(), 2, e2);
            }
        }
    };
    private final Process proc;

    protected static class Worker extends Thread {
        public int exit;
        public BufferedReader in;
        public OutputStreamWriter out;
        public Process proc;

        private Worker(Process proc, BufferedReader in, OutputStreamWriter out) {
            this.exit = -911;
            this.proc = proc;
            this.in = in;
            this.out = out;
        }

        public void run() {
            try {
                this.out.write("echo Started\n");
                this.out.flush();
                while (true) {
                    String line = this.in.readLine();
                    if (line == null) {
                        throw new EOFException();
                    } else if (!"".equals(line)) {
                        if ("Started".equals(line)) {
                            this.exit = 1;
                            return;
                        }
                        Shell.error = "unkown error occured.";
                    }
                }
            } catch (IOException e) {
                this.exit = -42;
                if (e.getMessage() != null) {
                    Shell.error = e.getMessage();
                } else {
                    Shell.error = "RootAccess denied?.";
                }
            }
        }
    }

    private Shell(String cmd) throws IOException, TimeoutException, RootDeniedException {
        RootTools.log("Starting shell: " + cmd);
        this.proc = new ProcessBuilder(new String[]{cmd}).redirectErrorStream(true).start();
        this.in = new BufferedReader(new InputStreamReader(this.proc.getInputStream()));
        this.out = new OutputStreamWriter(this.proc.getOutputStream(), HTTP.UTF_8);
        Worker worker = new Worker(this.proc, this.in, this.out);
        worker.start();
        try {
            worker.join((long) shellTimeout);
            if (worker.exit == -911) {
                this.proc.destroy();
                throw new TimeoutException(error);
            } else if (worker.exit == -42) {
                this.proc.destroy();
                throw new RootDeniedException("Root Access Denied");
            } else {
                new Thread(this.input, "Shell Input").start();
                new Thread(this.output, "Shell Output").start();
            }
        } catch (InterruptedException e) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
    }

    public Command add(Command command) throws IOException {
        if (this.close) {
            throw new IllegalStateException("Unable to add commands to a closed shell");
        }
        synchronized (this.commands) {
            this.commands.add(command);
            this.commands.notifyAll();
        }
        return command;
    }

    public void close() throws IOException {
        if (this == rootShell) {
            rootShell = null;
        }
        if (this == shell) {
            shell = null;
        }
        if (this == customShell) {
            customShell = null;
        }
        synchronized (this.commands) {
            this.close = true;
            this.commands.notifyAll();
        }
    }

    public int countCommands() {
        return this.commands.size();
    }

    public static void closeCustomShell() throws IOException {
        if (customShell != null) {
            customShell.close();
        }
    }

    public static void closeRootShell() throws IOException {
        if (rootShell != null) {
            rootShell.close();
        }
    }

    public static void closeShell() throws IOException {
        if (shell != null) {
            shell.close();
        }
    }

    public static void closeAll() throws IOException {
        closeShell();
        closeRootShell();
        closeCustomShell();
    }

    public static Shell getOpenShell() {
        if (customShell != null) {
            return customShell;
        }
        if (rootShell != null) {
            return rootShell;
        }
        return shell;
    }

    public static boolean isShellOpen() {
        if (shell == null) {
            return false;
        }
        return true;
    }

    public static boolean isCustomShellOpen() {
        if (customShell == null) {
            return false;
        }
        return true;
    }

    public static boolean isRootShellOpen() {
        if (rootShell == null) {
            return false;
        }
        return true;
    }

    public static boolean isAnyShellOpen() {
        if (shell == null && rootShell == null && customShell == null) {
            return false;
        }
        return true;
    }

    private void readOutput() throws IOException, InterruptedException {
        Command command = null;
        int read = 0;
        while (true) {
            String line = this.in.readLine();
            if (line == null) {
                break;
            }
            if (command == null) {
                if (read < this.commands.size()) {
                    command = (Command) this.commands.get(read);
                } else if (this.close) {
                    break;
                }
            }
            int pos = line.indexOf(token);
            if (pos > 0) {
                command.output(command.id, line.substring(0, pos));
            }
            if (pos >= 0) {
                line = line.substring(pos);
                String[] fields = line.split(" ");
                if (fields.length >= 2 && fields[1] != null) {
                    int id = 0;
                    try {
                        id = Integer.parseInt(fields[1]);
                    } catch (NumberFormatException e) {
                    }
                    int exitCode = -1;
                    try {
                        exitCode = Integer.parseInt(fields[2]);
                    } catch (NumberFormatException e2) {
                    }
                    if (id == read) {
                        command.setExitCode(exitCode);
                        read++;
                        command = null;
                    }
                }
            }
            command.output(command.id, line);
        }
        RootTools.log("Read all output");
        this.proc.waitFor();
        this.proc.destroy();
        RootTools.log("Shell destroyed");
        while (read < this.commands.size()) {
            if (command == null) {
                command = (Command) this.commands.get(read);
            }
            command.terminated("Unexpected Termination.");
            command = null;
            read++;
        }
    }

    public static void runRootCommand(Command command) throws IOException, TimeoutException, RootDeniedException {
        startRootShell().add(command);
    }

    public static void runCommand(Command command) throws IOException, TimeoutException {
        startShell().add(command);
    }

    public static Shell startRootShell() throws IOException, TimeoutException, RootDeniedException {
        return startRootShell(20000, 3);
    }

    public static Shell startRootShell(int timeout) throws IOException, TimeoutException, RootDeniedException {
        return startRootShell(timeout, 3);
    }

    public static Shell startRootShell(int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {
        shellTimeout = timeout;
        if (rootShell == null) {
            RootTools.log("Starting Root Shell!");
            String cmd = "su";
            int retries = 0;
            while (rootShell == null) {
                try {
                    rootShell = new Shell(cmd);
                } catch (IOException e) {
                    int retries2 = retries + 1;
                    if (retries >= retry) {
                        RootTools.log("IOException, could not start shell");
                        throw e;
                    }
                    retries = retries2;
                }
            }
        } else {
            RootTools.log("Using Existing Root Shell!");
        }
        return rootShell;
    }

    public static Shell startCustomShell(String shellPath) throws IOException, TimeoutException, RootDeniedException {
        return startCustomShell(shellPath, 20000);
    }

    public static Shell startCustomShell(String shellPath, int timeout) throws IOException, TimeoutException, RootDeniedException {
        shellTimeout = timeout;
        if (customShell == null) {
            RootTools.log("Starting Custom Shell!");
            customShell = new Shell(shellPath);
        } else {
            RootTools.log("Using Existing Custom Shell!");
        }
        return customShell;
    }

    public static Shell startShell() throws IOException, TimeoutException {
        return startShell(20000);
    }

    public static Shell startShell(int timeout) throws IOException, TimeoutException {
        shellTimeout = timeout;
        try {
            if (shell == null) {
                RootTools.log("Starting Shell!");
                shell = new Shell("/system/bin/sh");
            } else {
                RootTools.log("Using Existing Shell!");
            }
            return shell;
        } catch (RootDeniedException e) {
            throw new IOException();
        }
    }

    public void waitFor() throws IOException, InterruptedException {
        close();
        if (this.commands.size() > 0) {
            ((Command) this.commands.get(this.commands.size() - 1)).exitCode();
        }
    }

    private void writeCommands() throws IOException {
        int write = 0;
        while (true) {
            try {
                OutputStreamWriter out;
                synchronized (this.commands) {
                    while (!this.close && write >= this.commands.size()) {
                        this.commands.wait();
                    }
                    out = this.out;
                }
                if (write < this.commands.size()) {
                    ((Command) this.commands.get(write)).writeCommand(out);
                    out.write("\necho F*D^W@#FGF " + write + " $?\n");
                    out.flush();
                    write++;
                } else if (this.close) {
                    out.write("\nexit 0\n");
                    out.flush();
                    out.close();
                    RootTools.log("Closing shell");
                    return;
                }
            } catch (InterruptedException e) {
                RootTools.log(e.getMessage(), 2, e);
                return;
            }
        }
    }
}
