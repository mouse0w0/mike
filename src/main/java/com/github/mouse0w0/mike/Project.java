package com.github.mouse0w0.mike;

import com.moandjiezana.toml.Toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Project {

    public static final String MIKEFILE = "mike.toml";

    private final Path root;

    private String name;
    private List<Project> children;
    private List<Script> scripts;
    private List<Target> targets;

    // Options
    private String buildDir;
    private String installDir;
    private String cxx;
    private String cxxflags;
    private String ld;
    private String ldflags;
    private String ar;
    private String arflags;

    public Project(Path root) {
        this.root = root;
        parseProject(readConfig(root.resolve(MIKEFILE)));
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

    public List<Script> getScripts() {
        return scripts;
    }

    public String getBuildDir() {
        return buildDir;
    }

    public String getInstallDir() {
        return installDir;
    }

    public String getCxx() {
        return cxx;
    }

    public String getCxxflags() {
        return cxxflags;
    }

    public String getLd() {
        return ld;
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
        this.children = parseChildren(config.getList("children"));
        this.scripts = parseScripts(config.getTable("scripts"));
        this.targets = parseTargets(config.getTables("targets"));
        this.buildDir = config.getString("BUILD_DIR", "./build");
        this.installDir = config.getString("INSTALL_DIR", "/usr/local");
        this.cxx = config.getString("CXX", "g++");
        this.cxxflags = config.getString("CXXFLAGS", "-Wall");
        this.ld = config.getString("LD", "g++");
        this.ldflags = config.getString("LDFLAGS", "");
        this.ar = config.getString("AR", "ar");
        this.arflags = config.getString("ARFLAGS", "rc");
    }

    private List<Project> parseChildren(List<String> config) {
        List<Project> children = new ArrayList<>();
        if (config != null) {
            for (String c : config) {
                children.add(new Project(root.resolve(c)));
            }
        }
        return children;
    }

    private List<Script> parseScripts(Toml config) {
        List<Script> scripts = new ArrayList<>();
        if (config != null) {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                scripts.add(new Script(entry.getKey(), entry.getValue().toString().split("\r\n|\n|\r")));
            }
        }
        return scripts;
    }

    private List<Target> parseTargets(List<Toml> config) {
        List<Target> targets = new ArrayList<>();
        if (config != null) {
            for (Toml c : config) {
                targets.add(new Target(c));
            }
        }
        return targets;
    }
}
