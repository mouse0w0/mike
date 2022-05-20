package com.github.mouse0w0.mike;

import java.util.List;

public class Target {
    private String name;
    private List<String> sources;
    private List<String> includes;
    private List<String> libraries;
    private boolean executable;
    private boolean staticLibrary;
    private boolean sharedLibrary;

    public Target(String name, List<String> sources, List<String> includes, List<String> libraries, boolean executable, boolean staticLibrary, boolean sharedLibrary) {
        this.name = name;
        this.sources = sources;
        this.includes = includes;
        this.libraries = libraries;
        this.executable = executable;
        this.staticLibrary = staticLibrary;
        this.sharedLibrary = sharedLibrary;
    }

    public String getName() {
        return name;
    }

    public List<String> getSources() {
        return sources;
    }

    public List<String> getIncludes() {
        return includes;
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
