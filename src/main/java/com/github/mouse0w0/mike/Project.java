package com.github.mouse0w0.mike;

import com.moandjiezana.toml.Toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Project {

    private final Path root;

    private String name;
    private List<Project> children;
    private List<Target> targets;
    private String output;
    // Options
    private String cxx;
    private String cxxflags;
    private String ldflags;
    private String ar;
    private String arflags;

    public Project(Path root) {
        this.root = root;
        parseProject(readConfig(root.resolve(Constants.MIKEFILE)));
    }

    public Path getRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public List<Project> getChildren() {
        return children;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public String getOutput() {
        return output;
    }

    public String getCxx() {
        return cxx;
    }

    public String getCxxflags() {
        return cxxflags;
    }

    public String getLdflags() {
        return ldflags;
    }

    public String getAr() {
        return ar;
    }

    public String getArflags() {
        return arflags;
    }

    private Toml readConfig(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return new Toml().read(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void parseProject(Toml config) {
        this.name = config.getString("name", root.getFileName().toString());
        parseChildren(config.getList("children"));
        parseTargets(config.getTables("targets"));
        this.output = config.getString("output", "./build/");
        this.cxx = config.getString("cxx", "g++");
        this.cxxflags = config.getString("cxxflags", "-Wall");
        this.ldflags = config.getString("ldflags", "");
        this.ar = config.getString("ar", "ar");
        this.arflags = config.getString("arflags", "rc");
    }

    private void parseChildren(List<String> config) {
        children = new ArrayList<>();
        if (config == null) return;
        for (String c : config) {
            children.add(new Project(root.resolve(c)));
        }
    }

    private void parseTargets(List<Toml> config) {
        targets = new ArrayList<>();
        if (config == null) return;
        for (Toml c : config) {
            targets.add(new Target(c));
        }
    }
}
