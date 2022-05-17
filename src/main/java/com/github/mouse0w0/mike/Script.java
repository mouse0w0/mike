package com.github.mouse0w0.mike;

public class Script {
    private String name;
    private String[] commands;

    public Script(String name, String[] commands) {
        this.name = name;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public String[] getCommands() {
        return commands;
    }
}
