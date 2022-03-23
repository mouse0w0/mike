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
        writer.println("OUTPUT = " + project.getOutput());
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

        writer.print("clean:");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/clean");
        }
        writer.println();
        writer.println(".PHONY: clean");
        writer.println();

        writer.println("$(OUTPUT)%.o: %");
        writer.println("\t@mkdir -p $(dir $@)");
        writer.println("\t$(CXX) $(CXXFLAGS) -o $@ -c $<");
        writer.println();
    }

    private static void generateChildren(Project project, PrintWriter writer) {
        for (Project child : project.getChildren()) {
            generate(child);
        }
    }

    private static void generateTargets(Project project, PrintWriter writer) {
        for (Target target : project.getTargets()) {
            String name = target.getName();
            String uppercaseName = name.toUpperCase();
            writer.println("# --------------------------------------------------------------------------- ");
            writer.println("# TARGET " + uppercaseName);
            writer.println("# --------------------------------------------------------------------------- ");

            String varSources = uppercaseName + "_SOURCES";
            for (String source : target.getSources()) {
                if (source.isEmpty() || source.equals("./")) {
                    writer.println(varSources + " += ${wildcard *.c} ${wildcard *.cc} ${wildcard *.cpp} ");
                } else if (source.endsWith("/")) {
                    writer.println(varSources + " += ${wildcard " + source + "*.c} ${wildcard " + source + "*.cc} ${wildcard " + source + "*.cpp} ");
                } else {
                    writer.println(varSources + " += " + source);
                }
            }

            String varHeaders = uppercaseName + "_HEADERS";
            for (String header : target.getHeaders()) {
                if (header.isEmpty() || header.equals("./")) {
                    writer.println(varHeaders + " += ${wildcard *.h} ${wildcard *.hpp}");
                } else if (header.endsWith("/")) {
                    writer.println(varHeaders + " += ${wildcard " + header + "*.h} ${wildcard " + header + "*.hpp}");
                } else {
                    writer.println(varHeaders + " += " + header);
                }
            }

            String varObjects = uppercaseName + "_OBJECTS";
            writer.println(varObjects + " =  ${patsubst %, $(OUTPUT)%.o, $(" + varSources + ")}");

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
                writer.println("\t$(CXX) $(LDFLAGS) -o $(" + varExecutable + ") $(" + varObjects + ")");
            }

            if (target.isStaticLibrary()) {
                writer.println();
                writer.println(varStaticLibrary + " = " + name + ".a");
                writer.println(taskGenStaticLibrary + ": $(" + varObjects + ")");
                writer.println("\t$(AR) $(ARFLAGS) $(" + varStaticLibrary + ")$(" + varObjects + ")");
            }

            if (target.isSharedLibrary()) {
                writer.println();
                writer.println(varSharedLibrary + " = " + name + ".so");
                writer.println(taskGenSharedLibrary + ": $(" + varObjects + ")");
                writer.println("\t$(CXX) $(LDFLAGS) -o $(" + varSharedLibrary + ") -shared $(" + varObjects + ")");
            }

            String taskAll = name + "/all";
            writer.println();
            writer.print(taskAll + ":");
            if (target.isExecutable()) writer.print(" " + taskGenExecutable);
            if (target.isStaticLibrary()) writer.print(" " + taskGenStaticLibrary);
            if (target.isSharedLibrary()) writer.print(" " + taskGenSharedLibrary);
            writer.println();
            writer.println(".PHONY: " + taskAll);

            String taskClean = name + "/clean";
            writer.println();
            writer.println(taskClean + ":");
            writer.print("\trm -f $(" + varObjects + ")");
            if (target.isExecutable()) writer.print(" $(" + varExecutable + ")");
            if (target.isStaticLibrary()) writer.print(" $(" + varStaticLibrary + ")");
            if (target.isSharedLibrary()) writer.print(" $(" + varSharedLibrary + ")");
            writer.println();
            writer.println(".PHONY: " + taskClean);
        }
    }
}
