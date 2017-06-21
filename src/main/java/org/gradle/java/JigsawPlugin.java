/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.java;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.application.tasks.CreateStartScripts;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JigsawPlugin implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(JigsawPlugin.class);

    private static final String JAVA_PLUGIN = "org.gradle.java";

    private static final String JAVA_LIBRARY_PLUGIN = "org.gradle.java-library";

    private static final String APPLICATION_PLUGIN = "application";

    private static final String EXTENSION_NAME = "architecture";

    private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";

    @Override
    public void apply(Project project) {
        LOGGER.debug("Applying JigsawPlugin to " + project.getName());
        if (!project.getPlugins().hasPlugin(JAVA_PLUGIN) && !project.getPlugins().hasPlugin(JAVA_LIBRARY_PLUGIN)) {
            project.getPluginManager().apply(JavaPlugin.class);
        }
        project.getExtensions().create(EXTENSION_NAME, Architecture.class);

        configureJavaTasks(project);
    }

    private void configureJavaTasks(final Project project) {
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project project) {
                configureCompileJavaTask(project);
                configureCompileTestJavaTask(project);
                configureJarTask(project);
                configureTestTask(project);
                project.getPluginManager().withPlugin(APPLICATION_PLUGIN, new Action<AppliedPlugin>() {
                    @Override
                    public void execute(AppliedPlugin appliedPlugin) {
                        configureRunTask(project);
                        configureStartScriptsTask(project);
                    }
                });
            }
        });
    }

    private void configureCompileJavaTask(final Project project) {
        final JavaCompile compileJava = (JavaCompile) project.getTasks().findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        final Architecture module = (Architecture) project.getExtensions().getByName(EXTENSION_NAME);
        final String moduleDir =
                compileJava.getDestinationDir().toString() + System.getProperty("path.separator") + module.getModuleName();
        compileJava.getInputs().property("moduleName", module.getModuleName());
        compileJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(compileJava.getClasspath().getAsPath());
                args.add("-d");
                args.add(moduleDir);
                compileJava.getOptions().setCompilerArgs(args);
                compileJava.setClasspath(project.files());
            }
        });
        compileJava.doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {
                project.copy(new Action<CopySpec>() {
                    @Override
                    public void execute(CopySpec copySpec) {
                        copySpec.from(moduleDir);
                        copySpec.into(compileJava.getDestinationDir());
                    }
                });
            }
        });
    }

    private void configureCompileTestJavaTask(final Project project) {
        final JavaCompile compileTestJava = (JavaCompile) project.getTasks()
                .findByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME);
        final SourceSet test = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");
        final Architecture module = (Architecture) project.getExtensions().getByName(EXTENSION_NAME);
        compileTestJava.getInputs().property("moduleName", module.getModuleName());
        compileTestJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(compileTestJava.getClasspath().getAsPath());
                args.add("--add-modules");
                args.add("junit");
                args.add("--add-reads");
                args.add(module.getModuleName() + "=junit");
                args.add("--patch-module");
                args.add(module.getModuleName() + "=" + test.getJava().getSourceDirectories().getAsPath());
                compileTestJava.getOptions().setCompilerArgs(args);
                compileTestJava.setClasspath(project.files());
            }
        });
    }

    private void configureJarTask(final Project project) {
        final Jar jar = (Jar) project.getTasks().findByName(JavaPlugin.JAR_TASK_NAME);
        final Architecture module = (Architecture) project.getExtensions().getByName(EXTENSION_NAME);
        jar.getInputs().property("moduleName", module.getModuleName());
        jar.exclude(module.getModuleName());
    }

    private void configureTestTask(final Project project) {
        final Test testTask = (Test) project.getTasks().findByName(JavaPlugin.TEST_TASK_NAME);
        final SourceSet test = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");
        final Architecture module = (Architecture) project.getExtensions().getByName(EXTENSION_NAME);
        testTask.getInputs().property("moduleName", module.getModuleName());
        testTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(testTask.getClasspath().getAsPath());
                args.add("--add-modules");
                args.add("ALL-MODULE-PATH");
                args.add("--add-reads");
                args.add(module.getModuleName() + "=junit");
                args.add("--patch-module");
                args.add(module.getModuleName() + "=" + test.getJava().getOutputDir());
                testTask.setJvmArgs(args);
                testTask.setClasspath(project.files());
            }
        });
    }

    private void configureRunTask(final Project project) {
        final JavaExec run = (JavaExec) project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
        final Architecture module = (Architecture) project.getExtensions().getByName(EXTENSION_NAME);
        run.getInputs().property("moduleName", module.getModuleName());
        run.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(run.getClasspath().getAsPath());
                args.add("--module");
                args.add(module.getModuleName() + "/" + run.getMain());
                run.setJvmArgs(args);
                run.setClasspath(project.files());
            }
        });
    }

    private void configureStartScriptsTask(final Project project) {
        final CreateStartScripts startScripts = (CreateStartScripts) project.getTasks()
                .findByName(ApplicationPlugin.TASK_START_SCRIPTS_NAME);
        final Architecture module = (Architecture) project.getExtensions().getByName(EXTENSION_NAME);
        startScripts.getInputs().property("moduleName", module.getModuleName());
        startScripts.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                startScripts.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(LIBS_PLACEHOLDER);
                args.add("--module");
                args.add(module.getModuleName() + "/" + startScripts.getMainClassName());
                startScripts.setDefaultJvmOpts(args);
            }
        });
        startScripts.doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {
                File bashScript = new File(startScripts.getOutputDir(), startScripts.getApplicationName());
                replaceLibsPlaceHolder(bashScript.toPath(), "\\$APP_HOME/lib");
                File batFile = new File(startScripts.getOutputDir(), startScripts.getApplicationName() + ".bat");
                replaceLibsPlaceHolder(batFile.toPath(), "%APP_HOME%\\lib");
            }
        });
    }

    private void replaceLibsPlaceHolder(Path path, String newText) {
        try {
            List<String> bashContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
            for (int i=0; i < bashContent.size(); i++) {
                bashContent.set(i, bashContent.get(i).replaceFirst(LIBS_PLACEHOLDER, newText));
            }
            Files.write(path, bashContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GradleException("Couldn't replace placeholder in " + path);
        }
    }
}
