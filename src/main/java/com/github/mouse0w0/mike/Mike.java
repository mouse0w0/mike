package com.github.mouse0w0.mike;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Mike {
    public static void main(String[] args) {
        Path dir;
        if (args.length == 0) {
            dir = Paths.get(System.getProperty("user.dir"));
        } else if (args.length == 1) {
            dir = Paths.get(args[0]).toAbsolutePath();
        } else {
            System.out.println("Too many arguments, usage 'mike [folder]'");
            return;
        }
        System.out.println("Generating Makefile at " + dir);
        Path mikefile = dir.resolve(Project.MIKEFILE);
        if (Files.notExists(mikefile)) {
            System.out.println("No found " + mikefile + ", stop generating");
            return;
        }
        Generator.generate(new Project(dir));
        System.out.println("Generating Makefile successfully");
    }
}
