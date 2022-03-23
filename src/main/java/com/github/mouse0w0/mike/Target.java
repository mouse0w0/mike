package com.github.mouse0w0.mike;

import com.moandjiezana.toml.Toml;

import java.util.Collections;
import java.util.List;

public class Target {
    private String name;
    private List<String> sources;
    private List<String> headers;
    private List<String> libraries;
    private boolean executable;
    private boolean staticLibrary;
    private boolean sharedLibrary;

    public Target(Toml config) {
        this.name = config.getString("name");
        this.sources = config.contains("sources") ? config.getList("sources") : Collections.singletonList("./");
        this.headers = config.contains("headers") ? config.getList("headers") : Collections.singletonList("./");
        this.libraries = config.contains("libraries") ? config.getList("libraries") : Collections.emptyList();
        this.executable = config.getBoolean("executable", false);
        this.staticLibrary = config.getBoolean("staticLibrary", false);
        this.sharedLibrary = config.getBoolean("sharedLibrary", false);
        if (!(executable || staticLibrary || sharedLibrary)) this.executable = true;
    }

    public String getName() {
        return name;
    }

    public List<String> getSources() {
        return sources;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public boolean isExecutable() {
        return executable;
    }

    public boolean isStaticLibrary() {
        return staticLibrary;
    }

    public boolean isSharedLibrary() {
        return sharedLibrary;
    }
}
