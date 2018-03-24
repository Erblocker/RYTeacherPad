package com.stericson.RootTools.execution;

import com.stericson.RootTools.RootTools;

public class CommandCapture extends Command {
    private StringBuilder sb = new StringBuilder();

    public CommandCapture(int id, String... command) {
        super(id, command);
    }

    public void output(int id, String line) {
        this.sb.append(line).append('\n');
        RootTools.log("Command", "ID: " + id + ", " + line);
    }

    public String toString() {
        return this.sb.toString();
    }
}
