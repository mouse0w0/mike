package com.github.mouse0w0.mike;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Generator {
    public static final String MAKEFILE = "Makefile";

    public static void generate(Project project) {
        PrintWriter writer = null;
        try {
            Path makefile = project.getRoot().resolve(MAKEFILE);
            Files.deleteIfExists(makefile);
            writer = new PrintWriter(Files.newBufferedWriter(makefile, StandardOpenOption.CREATE));
            generateHeader(project, writer);
            generateProjectOptions(project, writer);
            generateProjectTasks(project, writer);
            generateScripts(project, writer);
            generateTargets(project, writer);
            generateTests(project, writer);
            generateHelp(project, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void generateHeader(Project project, PrintWriter writer) {
        writer.println("SHELL=/bin/bash");
    }

    private static void generateProjectOptions(Project project, PrintWriter writer) {
        Options options = project.getOptions();
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# OPTIONS");
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("BUILD_DIR = " + options.getBuildDir());
        writer.println("INSTALL_DIR = " + options.getInstallDir());
        writer.println("CXX = " + options.getCxx());
        writer.println("CXXFLAGS = " + options.getCxxflags());
        writer.println("LD = " + options.getLd());
        writer.println("LDFLAGS = " + options.getLdflags());
        writer.println("AR = " + options.getAr());
        writer.println("ARFLAGS = " + options.getArflags());
        writer.println();
    }

    private static void generateProjectTasks(Project project, PrintWriter writer) {
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# TASKS");
        writer.println("# --------------------------------------------------------------------------- ");

        generateProjectTask(project, writer, "all");
        generateProjectTask(project, writer, "clean");
        generateProjectTask(project, writer, "depend");
        generateProjectTask(project, writer, "install");
        generateProjectTask(project, writer, "uninstall");
    }

    private static void generateProjectTask(Project project, PrintWriter writer, String task) {
        writer.print(task + ":");
        for (Target target : project.getTargets()) {
            writer.print(" " + target.getName() + "/" + task);
        }
        writer.println();
        writer.println(".PHONY: " + task);
        writer.println();
    }

    private static void generateScripts(Project project, PrintWriter writer) {
        if (project.getScripts().isEmpty()) return;
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# SCRIPTS");
        writer.println("# --------------------------------------------------------------------------- ");
        for (Script script : project.getScripts()) {
            String task = "run/" + script.getName();
            writer.println(task + ":");
            for (String command : script.getCommands()) {
                writer.print("\t");
                writer.println(command);
            }
            writer.println(".PHONY: " + task);
            writer.println();
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

            String varDepend = uppercaseName + "_DEPEND";
            writer.println(varDepend + " = $(" + varBuildDir + ")/" + name + ".d");
            writer.println("-include $(" + varDepend + ")");

            String varSources = uppercaseName + "_SOURCES";
            for (String source : target.getSources()) {
                if (isCurrentPath(source)) {
                    writer.println(varSources + " += $(wildcard *.c) $(wildcard *.cc) $(wildcard *.cpp)");
                } else if (Files.isDirectory(root.resolve(source))) {
                    if (source.endsWith("/")) source = source.substring(0, source.length() - 1);
                    writer.println(varSources + " += $(wildcard " + source + "/*.c) $(wildcard " + source + "/*.cc) $(wildcard " + source + "/*.cpp)");
                } else if (isSourceFile(source)) {
                    writer.println(varSources + " += " + source);
                }
            }

            String varHeaders = uppercaseName + "_HEADERS";
            for (String source : target.getSources()) {
                if (isCurrentPath(source)) {
                    writer.println(varHeaders + " += $(wildcard *.h) $(wildcard *.hpp)");
                } else if (Files.isDirectory(root.resolve(source))) {
                    if (source.endsWith("/")) source = source.substring(0, source.length() - 1);
                    writer.println(varHeaders + " += $(wildcard " + source + "*.h) $(wildcard " + source + "*.hpp)");
                } else if (isHeaderFile(source)) {
                    writer.println(varHeaders + " += " + source);
                }
            }

            String varObjects = uppercaseName + "_OBJECTS";
            writer.println(varObjects + " = $(patsubst %, $(" + varBuildDir + ")/%.o, $(" + varSources + "))");

            String varIncludes = uppercaseName + "_INCLUDES";
            writer.print(varIncludes + " =");
            for (String include : target.getIncludes()) {
                writer.print(" " + include);
            }
            writer.println();

            String varIncludeFlags = uppercaseName + "_INCLUDE_FLAGS";
            writer.println(varIncludeFlags + " = $(addprefix -I,$(" + varIncludes + "))");

            String varLibraries = uppercaseName + "_LIBS";
            writer.println(varLibraries + " =");
            for (String library : target.getLibraries()) {
                writer.println(" " + library);
            }

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
                writer.println("\t$(LD) $(LDFLAGS) -o $@ $(" + varObjects + ") $(" + varLibraries + ")");
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

            String taskClean = name + "/clean";
            writer.println();
            writer.println(taskClean + ":");
            writer.println("\t@echo Cleaning project $(shell pwd)");
            writer.print("\t@rm -f $(" + varObjects + ") $(" + varDepend + ")");
            if (target.isExecutable()) writer.print(" $(" + varExecutable + ")");
            if (target.isStaticLibrary()) writer.print(" $(" + varStaticLibrary + ")");
            if (target.isSharedLibrary()) writer.print(" $(" + varSharedLibrary + ")");
            writer.println();
            writer.println("\t@echo Cleaned project");
            writer.println(".PHONY: " + taskClean);

            String taskDepend = name + "/depend";
            writer.println();
            writer.println(taskDepend + ":");
            writer.println("\t@mkdir -p $(dir $(" + varDepend + "))");
            writer.println("\t$(CXX) -MM $(" + varSources + ") > $(" + varDepend + ")");
            writer.println("\t@sed -i -E \"s|^(.+?).o: ([^ ]+?)|$(" + varBuildDir + ")/\\2.o: \\2|g\" $(" + varDepend + ")");
            writer.println(".PHONY: " + taskDepend);

            String taskInstall = name + "/install";
            writer.println();
            writer.println(taskInstall + ": " + taskAll);
            writer.println("\t@echo Installing project $(shell pwd)");
            writer.println("\t@if [ -n \"$(" + varHeaders + ")\" ]; then cp $(" + varHeaders + ") $(INSTALL_DIR)/include/; fi;");
            if (target.isExecutable()) writer.println("\t@cp $(" + varExecutable + ") $(INSTALL_DIR)/bin/");
            if (target.isStaticLibrary()) writer.println("\t@cp $(" + varStaticLibrary + ") $(INSTALL_DIR)/lib/");
            if (target.isSharedLibrary()) writer.println("\t@cp $(" + varSharedLibrary + ") $(INSTALL_DIR)/lib/");
            writer.println("\t@echo Installed project");
            writer.println(".PHONY: " + taskInstall);

            String taskUninstall = name + "/uninstall";
            writer.println();
            writer.println(taskUninstall + ":");
            writer.println("\t@echo Uninstalling project $(shell pwd)");
            writer.print("\t@rm -f $(addprefix $(INSTALL_DIR)/include/,$(notdir $(" + varHeaders + ")))");
            if (target.isExecutable()) writer.print(" $(INSTALL_DIR)/bin/$(" + varExecutable + ")");
            if (target.isStaticLibrary()) writer.print(" $(INSTALL_DIR)/lib/$(" + varStaticLibrary + ")");
            if (target.isSharedLibrary()) writer.print(" $(INSTALL_DIR)/lib/$(" + varSharedLibrary + ")");
            writer.println();
            writer.println("\t@echo Uninstalled project");
            writer.println(".PHONY: " + taskUninstall);

            writer.println();
            writer.println("$(" + varBuildDir + ")/%.o: %");
            writer.println("\t@mkdir -p $(dir $@)");
            writer.println("\t$(CXX) $(CXXFLAGS) $(" + varIncludeFlags + ") -o $@ -c $<");
            writer.println();
        }
    }

    private static void generateTests(Project project, PrintWriter writer) {
        if (project.getTests().isEmpty()) return;
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# TESTS");
        writer.println("# --------------------------------------------------------------------------- ");
        String testTask = "test";
        writer.println(testTask + ": TEST_TOTAL = " + project.getTests().size());
        writer.println(testTask + ":");
        writer.println("\t@echo Test project $(shell pwd)");
        int testCurrent = 1;
        for (Test test : project.getTests()) {
            String task = "test/" + test.getName();
            writer.println("\t@$(MAKE) --no-print-directory " + task + " TEST_TOTAL=$(TEST_TOTAL) TEST_CURRENT=" + testCurrent++);
        }
        writer.println("\t@echo Test project finished");
        writer.println(".PHONY: " + testTask);
        writer.println();

        writer.println("TEST_TOTAL ?= 1");
        writer.println("TEST_CURRENT ?= 1");
        writer.println();

        for (Test test : project.getTests()) {
            String task = "test/" + test.getName();
            writer.println(task + ": TEST_NAME = " + test.getName());
            writer.println(task + ": TEST_TARGET = " + test.getTarget());
            if (isNotEmpty(test.getArgs())) {
                writer.println(task + ": TEST_ARGS = " + test.getArgs());
            }
            if (isNotEmpty(test.getInput())) {
                writer.println(task + ": TEST_INPUT = " + test.getInput());
            }
            writer.println(task + ": TEST_EXPECT = " + test.getExpect());
            writer.println(task + ": " + test.getTarget());
            writer.println("\t@echo -e \"\\tStart $(TEST_CURRENT)\\t: $(TEST_NAME)\"");
            writer.print("\t$(eval TEST_OUTPUT=$(shell ./$(TEST_TARGET)");
            if (isNotEmpty(test.getArgs())) {
                writer.print(" $(TEST_ARGS)");
            }
            if (isNotEmpty(test.getInput())) {
                writer.print(" <<< " + test.getInput());
            }
            writer.println("))");
            writer.println("\t$(eval TEST_RESULT=$(shell if [ \"$(TEST_OUTPUT)\" == \"$(TEST_EXPECT)\" ]; then echo Passed; else echo Failed; fi;))");
            writer.println("\t@echo -e \"$(TEST_CURRENT)/$(TEST_TOTAL)\\tTest  $(TEST_CURRENT)\\t: $(TEST_NAME)\\t................   $(TEST_RESULT)\"");
            writer.println(".PHONY: " + task);
            writer.println();
        }
    }

    private static void generateHelp(Project project, PrintWriter writer) {
        String helpTask = "help";
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println("# HELP");
        writer.println("# --------------------------------------------------------------------------- ");
        writer.println(helpTask + ":");
        writer.println("\t@echo \"The following are some of the valid targets for this Makefile:\"");
        writer.println("\t@echo \"... all (the default if no target is provided)\"");
        writer.println("\t@echo \"... clean\"");
        writer.println("\t@echo \"... depend\"");
        writer.println("\t@echo \"... install\"");
        writer.println("\t@echo \"... uninstall\"");
        writer.println("\t@echo \"... test\"");
        for (Script script : project.getScripts()) {
            writer.println("\t@echo \"... run/" + script.getName() + "\"");
        }
        for (Target target : project.getTargets()) {
            if (target.isExecutable()) {
                writer.println("\t@echo \"... " + target.getName() + "\"");
            }
            if (target.isStaticLibrary()) {
                writer.println("\t@echo \"... " + target.getName() + ".a\"");
            }
            if (target.isSharedLibrary()) {
                writer.println("\t@echo \"... " + target.getName() + ".so\"");
            }
        }
        writer.println(".PHONY: " + helpTask);
    }

    private static void generateChildrenTargets(Project project, PrintWriter writer) {

    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static boolean isCurrentPath(String path) {
        return path.isEmpty() || path.equals(".") || path.equals("./");
    }

    private static boolean isSourceFile(String file) {
        return file.endsWith(".c") || file.endsWith(".cc") || file.endsWith(".cpp");
    }

    private static boolean isHeaderFile(String file) {
        return file.endsWith(".h") || file.endsWith(".hpp");
    }
}
