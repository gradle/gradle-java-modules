package org.gradle.java.tests;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.java.JavaModule;
import org.gradle.java.JigsawPlugin;
import org.gradle.java.TestEngine;

import java.util.ArrayList;
import java.util.List;

import static org.gradle.java.tests.TestEngineUtils.createModuleConfiguration;
import static org.gradle.java.tests.TestEngineUtils.createReadModuleConfiguration;

public class JUnit4 implements TestEngine {
    private static final Logger LOGGER = Logging.getLogger(JigsawPlugin.class);
    private static final String JUNIT_MODULE_NAME = "junit";

    @Override
    public boolean accepts(Project project) {
        Configuration configuration = project.getConfigurations().getByName("testCompile");
        for(Dependency dependency: configuration.getDependencies()) {
            if (dependency.getGroup().equals("junit") && dependency.getName().equals("junit")) {
                LOGGER.debug("Found JUnit 4 dependency - using JUnit 4 Jigsaw test engine configuration");
                return true;
            }
        }
        return false;
    }

    @Override
    public Action<Task> createCompileTestJavaAction(final Project project, final SourceSet test, final JavaCompile compileTestJava, final JavaModule module) {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(compileTestJava.getClasspath().getAsPath());
                createModuleConfiguration(args, module, JUNIT_MODULE_NAME);
                args.add("--patch-module");
                args.add(module.geName() + "=" + test.getJava().getSourceDirectories().getAsPath());
                compileTestJava.getOptions().setCompilerArgs(args);
                compileTestJava.setClasspath(project.files());
            }
        };
    }

    @Override
    public Action<Task> createTestJavaAction(final Project project, final SourceSet test, final Test testTask, final JavaModule module) {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(testTask.getClasspath().getAsPath());
                args.add("--add-modules");
                args.add("ALL-MODULE-PATH");
                createReadModuleConfiguration(args, module, JUNIT_MODULE_NAME);
                args.add("--patch-module");
                args.add(module.geName() + "=" + test.getJava().getOutputDir());
                testTask.setJvmArgs(args);
                testTask.setClasspath(project.files());
            }
        };
    }
}
