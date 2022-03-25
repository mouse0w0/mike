package com.github.mouse0w0.mike;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Generator {
    public static void generate(Project project) {
        PrintWriter writer = null;
        try {
            Path makefile = project.getRoot().resolve(Constants.MAKEFILE);
            Files.deleteIfExists(makefile);
            writer = new PrintWriter(Files.newBufferedWriter(makefile, StandardOpenOption.CREATE));
            generateProject(project, writer);
            generateChildren(project, writer);
            generateTargets(project, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void generateProject(Project project, PrintWriter writer) {
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# OPTIONS");
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("BUILD_DIR = " + project.getBuildDir());
        writer.println("INSTALL_DIR = " + project.getInstallDir());
        writer.println("CXX = " + project.getCxx());
        writer.println("CXXFLAGS = " + project.getCxxflags());
        writer.println("LDFLAGS = " + project.getLdflags());
        writer.println("AR = " + project.getAr());
        writer.println("ARFLAGS = " + project.getArflags());
        writer.println();

        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# TASKS");
        writer.println("# --------------------------------------------------------------------------- ");

        writer.print("all:");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/all");
        }
        writer.println();
        writer.println(".PHONY: all");
        writer.println();

        writer.print("depend:");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/depend");
        }
        writer.println();
        writer.println(".PHONY: depend");
        writer.println();

        writer.print("install:");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/install");
        }
        writer.println();
        writer.println(".PHONY: install");
        writer.println();

        writer.print("uninstall:");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/uninstall");
        }
        writer.println();
        writer.println(".PHONY: uninstall");
        writer.println();

        writer.print("clean:");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/clean");
        }
        writer.println();
        writer.println(".PHONY: clean");
        writer.println();
    }

    private static void generateChildren(Project project, PrintWriter writer) {
        for (Project child : project.getChildren()) {
            generate(child);
        }
    }

    private static void generateTargets(Project project, PrintWriter writer) {
        Path root = project.getRoot();
        for (Target target : project.getTargets()) {
            String name = target.getName();
            String uppercaseName = name.toUpperCase();
            writer.println("# --------------------------------------------------------------------------- ");
            writer.println("# TARGET " + uppercaseName);
            writer.println("# --------------------------------------------------------------------------- ");

            String varBuildDir = uppercaseName + "_BUILD_DIR";
            writer.println(varBuildDir + " = $(BUILD_DIR)/" + name);

            String varSources = uppercaseName + "_SOURCES";
            for (String source : target.getSources()) {
                if (isCurrentPath(source)) {
                    writer.println(varSources + " += $(wildcard *.c) $(wildcard *.cc) $(wildcard *.cpp)");
                } else if (Files.isDirectory(root.resolve(source))) {
                    if (source.endsWith("/")) source = source.substring(0, source.length() - 1);
                    writer.println(varSources + " += $(wildcard " + source + "/*.c) $(wildcard " + source + "/*.cc) $(wildcard " + source + "/*.cpp)");
                } else {
                    writer.println(varSources + " += " + source);
                }
            }

            String varHeaders = uppercaseName + "_HEADERS";
            for (String header : target.getHeaders()) {
                if (isCurrentPath(header)) {
                    writer.println(varHeaders + " += $(wildcard *.h) $(wildcard *.hpp)");
                } else if (Files.isDirectory(root.resolve(header))) {
                    if (header.endsWith("/")) header = header.substring(0, header.length() - 1);
                    writer.println(varHeaders + " += $(wildcard " + header + "*.h) $(wildcard " + header + "*.hpp)");
                } else {
                    writer.println(varHeaders + " += " + header);
                }
            }

            String varObjects = uppercaseName + "_OBJECTS";
            writer.println(varObjects + " = $(patsubst %, $(" + varBuildDir + ")/%.o, $(" + varSources + "))");

            String varDepend = uppercaseName + "_DEPEND";
            writer.println(varDepend + " = $(" + varBuildDir + ")/" + name + ".d");
            writer.println("-include $(" + varDepend + ")");

            String varExecutable = uppercaseName + "_EXECUTABLE";
            String varStaticLibrary = uppercaseName + "_STATIC_LIB";
            String varSharedLibrary = uppercaseName + "_SHARED_LIB";

            String taskGenExecutable = name;
            String taskGenStaticLibrary = name + ".a";
            String taskGenSharedLibrary = name + ".so";

            if (target.isExecutable()) {
                writer.println();
                writer.println(varExecutable + " = " + name);
                writer.println(taskGenExecutable + ": $(" + varObjects + ")");
                writer.println("\t$(CXX) $(LDFLAGS) -o $@ $(" + varObjects + ")");
            }

            if (target.isStaticLibrary()) {
                writer.println();
                writer.println(varStaticLibrary + " = " + name + ".a");
                writer.println(taskGenStaticLibrary + ": $(" + varObjects + ")");
                writer.println("\t$(AR) $(ARFLAGS) $@ $(" + varObjects + ")");
            }

            if (target.isSharedLibrary()) {
                writer.println();
                writer.println(varSharedLibrary + " = " + name + ".so");
                writer.println(taskGenSharedLibrary + ": $(" + varObjects + ")");
                writer.println("\t$(CXX) $(LDFLAGS) -o $@ -shared $(" + varObjects + ")");
            }

            String taskAll = name + "/all";
            writer.println();
            writer.print(taskAll + ":");
            if (target.isExecutable()) writer.print(" " + taskGenExecutable);
            if (target.isStaticLibrary()) writer.print(" " + taskGenStaticLibrary);
            if (target.isSharedLibrary()) writer.print(" " + taskGenSharedLibrary);
            writer.println();
            writer.println(".PHONY: " + taskAll);

            String taskDepend = name + "/depend";
            writer.println();
            writer.println(taskDepend + ":");
            writer.println("\t@mkdir -p $(dir $(" + varDepend + "))");
            writer.println("\t$(CXX) -MM $(" + varSources + ") > $(" + varDepend + ")");
            writer.println("\t@sed -i -E \"s|^(.+?).o: ([^ ]+?)|$(" + varBuildDir + ")/\\2.o: \\2|g\" $(" + varDepend + ")");
            writer.println(".PHONY: " + taskDepend);

            String taskClean = name + "/clean";
            writer.println();
            writer.println(taskClean + ":");
            writer.print("\trm -f $(" + varObjects + ") $(" + varDepend + ")");
            if (target.isExecutable()) writer.print(" $(" + varExecutable + ")");
            if (target.isStaticLibrary()) writer.print(" $(" + varStaticLibrary + ")");
            if (target.isSharedLibrary()) writer.print(" $(" + varSharedLibrary + ")");
            writer.println();
            writer.println(".PHONY: " + taskClean);

            String taskInstall = name + "/install";
            writer.println();
            writer.println(taskInstall + ": " + taskAll);
            writer.println("\tcp $(" + varHeaders + ") $(INSTALL_DIR)/include");
            if (target.isExecutable()) writer.println("\tcp $(" + varExecutable + ") $(INSTALL_DIR)/bin");
            if (target.isStaticLibrary()) writer.println("\tcp $(" + varStaticLibrary + ") $(INSTALL_DIR)/lib");
            if (target.isSharedLibrary()) writer.println("\tcp $(" + varSharedLibrary + ") $(INSTALL_DIR)/lib");
            writer.println(".PHONY: " + taskInstall);

            String taskUninstall = name + "/uninstall";
            writer.println();
            writer.println(taskUninstall + ":");
            writer.println("\trm -f $(addprefix $(INSTALL_DIR)/include/,$(notdir $(" + varHeaders + ")))");
            if (target.isExecutable()) writer.println("\trm -f $(INSTALL_DIR)/bin/$(" + varExecutable + ")");
            if (target.isStaticLibrary()) writer.println("\trm -f $(INSTALL_DIR)/lib/$(" + varStaticLibrary + ")");
            if (target.isSharedLibrary()) writer.println("\trm -f $(INSTALL_DIR)/lib/$(" + varSharedLibrary + ")");
            writer.println(".PHONY: " + taskUninstall);

            writer.println();
            writer.println("$(" + varBuildDir + ")/%.o: %");
            writer.println("\t@mkdir -p $(dir $@)");
            writer.println("\t$(CXX) $(CXXFLAGS) -o $@ -c $<");
            writer.println();
        }
    }

    private static boolean isCurrentPath(String path) {
        return path.isEmpty() || path.equals(".") || path.equals("./");
    }
}
