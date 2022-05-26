package com.github.mouse0w0.mike;

import com.moandjiezana.toml.Toml;

public class Options {
    private String buildDir;
    private String installDir;
    private String cxx;
    private String cxxflags;
    private String ld;
    private String ldflags;
    private String ar;
    private String arflags;

    public Options(Toml config) {
        this.buildDir = config.getString("BUILD_DIR", "./build");
        this.installDir = config.getString("INSTALL_DIR", "/usr/local");
        this.cxx = config.getString("CXX", "g++");
        this.cxxflags = config.getString("CXXFLAGS", "-Wall");
        this.ld = config.getString("LD", "g++");
        this.ldflags = config.getString("LDFLAGS", "");
        this.ar = config.getString("AR", "ar");
        this.arflags = config.getString("ARFLAGS", "rc");
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
}
