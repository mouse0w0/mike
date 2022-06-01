package com.github.mouse0w0.mike;

import com.moandjiezana.toml.Toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Project {

    public static final String MIKEFILE = "mike.toml";

    private Path root;
    private Options options;
    private List<Script> scripts;
    private List<Target> targets;
    private List<Test> tests;

    public Path getRoot() {
        return root;
    }

    public Options getOptions() {
        return options;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public List<Test> getTests() {
        return tests;
    }

    public static Project parseProject(Path root) {
        Project project = new Project();
        Toml config = readConfig(root.resolve(MIKEFILE));
        project.root = root;
        project.options = new Options(config);
        project.scripts = parseScripts(config.getTable("scripts"));
        project.targets = parseTargets(config.getTable("targets"));
        project.tests = parseTests(config.getTable("tests"));
        return project;
    }

    private static Toml readConfig(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return new Toml().read(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Script> parseScripts(Toml config) {
        List<Script> scripts = new ArrayList<>();
        if (config != null) {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                scripts.add(new Script(entry.getKey(), entry.getValue().toString().trim().split("(\\r\\n|\\n|\\r)\\s*")));
            }
        }
        return scripts;
    }

    private static List<Target> parseTargets(Toml config) {
        List<Target> targets = new ArrayList<>();
        if (config != null) {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String name = entry.getKey();
                Toml table = config.getTable(name);
                List<String> sources = table.contains("sources") ? table.getList("sources") : Collections.singletonList(".");
                List<String> includes = table.contains("includes") ? table.getList("includes") : Collections.emptyList();
                List<String> libraries = table.contains("libraries") ? table.getList("libraries") : Collections.emptyList();
                boolean executable = table.getBoolean("executable", false);
                boolean staticLibrary = table.getBoolean("staticLibrary", false);
                boolean sharedLibrary = table.getBoolean("sharedLibrary", false);
                if (!(executable || staticLibrary || sharedLibrary)) executable = true;
                targets.add(new Target(name, sources, includes, libraries, executable, staticLibrary, sharedLibrary));
            }
        }
        return targets;
    }

    private static List<Test> parseTests(Toml config) {
        List<Test> tests = new ArrayList<>();
        if (config != null) {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String name = entry.getKey();
                Toml table = config.getTable(name);
                String target = table.getString("target");
                String args = table.getString("args");
                String input = table.getString("input");
                String expect = table.getString("expect");
                tests.add(new Test(name, target, args, input, expect));
            }
        }
        return tests;
    }
}
