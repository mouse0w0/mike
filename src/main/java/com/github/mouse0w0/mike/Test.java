package com.github.mouse0w0.mike;

public class Test {
    private String name;
    private String target;
    private String args;
    private String input;
    private String expect;

    public Test(String name, String target, String args, String input, String expect) {
        this.name = name;
        this.target = target;
        this.args = args;
        this.input = input;
        this.expect = expect;
    }

    public String getName() {
        return name;
    }

    public String getTarget() {
        return target;
    }

    public String getArgs() {
        return args;
    }

    public String getInput() {
        return input;
    }

    public String getExpect() {
        return expect;
    }
}
