/*
 * Copyright 2009 the original author or authors.
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
import java.util.Set;

public class JigsawPlugin implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(JigsawPlugin.class);

    private static final String JAVA_PLUGIN = "org.gradle.java";

    private static final String JAVA_LIBRARY_PLUGIN = "org.gradle.java-library";

    private static final String APPLICATION_PLUGIN = "application";

    private static final String EXTENSION_NAME = "jigsawModule";

    private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";

    @Override
    public void apply(Project project) {
        LOGGER.debug("Applying JigsawPlugin to " + project.getName());

        project.getExtensions().create(EXTENSION_NAME, JigsawModule.class);

        configureJavaTasks(project);
    }

    private void configureJavaTasks(Project project) {
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                if (project.getPlugins().hasPlugin(JAVA_PLUGIN)
                        || project.getPlugins().hasPlugin(JAVA_LIBRARY_PLUGIN)) {
                    configureCompileJavaTask(project);
                    configureCompileTestJavaTask(project);
                    configureJarTask(project);
                    configureTestTask(project);
                } else {
                    throw new GradleException("You must apply the java or java-library plugin as well.");
                }
                if (project.getPlugins().hasPlugin(APPLICATION_PLUGIN)) {
                    configureRunTask(project);
                    configureStartScriptsTask(project);
                }
            }
        });
    }

    private void configureRunTask(Project project) {
        final JavaExec run = getRunTask(project);
        final SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName(EXTENSION_NAME);
        run.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                run.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(project.files(
                        main.getJava().getOutputDir(),
                        project.getConfigurations().getByName("runtimeClasspath")).getAsPath());
                args.add("--module");
                args.add(String.format("%s/%s", module.getName(), run.getMain()));
                run.setJvmArgs(args);
            }
        });
    }

    private void configureStartScriptsTask(Project project) {
        final CreateStartScripts startScripts = getStartScriptsTask(project);
        final SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
        final SourceSet test = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName(EXTENSION_NAME);
        startScripts.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                startScripts.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(LIBS_PLACEHOLDER);
                args.add("--module");
                args.add(String.format("%s/%s", module.getName(), startScripts.getMainClassName()));
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
            throw new GradleException(String.format("Couldn't replace placeholder in  %s", path));
        }
    }

    private void configureTestTask(Project project) {
        final Test testTask = getTestTask(project);
        final SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
        final SourceSet test = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName(EXTENSION_NAME);
        testTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                testTask.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(project.files(
                        main.getJava().getOutputDir(),
                        project.getConfigurations().getByName("testRuntimeClasspath")).getAsPath());
                args.add("--add-modules");
                args.add("ALL-MODULE-PATH");
                args.add("--add-reads");
                args.add(String.format("%s=junit", module.getName()));
                args.add("--patch-module");
                args.add(String.format("%s=%s", module.getName(), test.getJava().getOutputDir()));
                testTask.setJvmArgs(args);
            }
        });
    }

    private void configureJarTask(final Project project) {
        final Jar jar = getJarTask(project);
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName(EXTENSION_NAME);
        jar.exclude(module.getName());
    }

    private void configureCompileJavaTask(final Project project) {
        final JavaCompile compileJava = getCompileJavaTask(project);
        final SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName(EXTENSION_NAME);
        final String moduleDir = String.format("%s%s%s",
                main.getJava().getOutputDir(),
                System.getProperty("path.separator"),
                module.getName());
        compileJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                compileJava.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(project.getConfigurations().getByName("compileClasspath").getAsPath());
                args.add("--source-path");
                args.add(project.files(main.getJava().getSourceDirectories()).getAsPath());
                args.add("-d");
                args.add(moduleDir);
                compileJava.getOptions().setCompilerArgs(args);
            }
        });
        compileJava.doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {
                project.copy(new Action<CopySpec>() {
                    @Override
                    public void execute(CopySpec copySpec) {
                        copySpec.from(moduleDir);
                        copySpec.into(main.getJava().getOutputDir());
                    }
                });
            }
        });
    }

    private void configureCompileTestJavaTask(final Project project) {
        final JavaCompile compileTestJava = getCompileTestJavaTask(project);
        final SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
        final SourceSet test = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName(EXTENSION_NAME);
        compileTestJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                compileTestJava.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(project.files(
                        main.getJava().getOutputDir(),
                        project.getConfigurations().getByName("testCompileClasspath")).getAsPath());
                args.add("--add-modules");
                args.add("junit");
                args.add("--add-reads");
                args.add(String.format("%s=junit", module.getName()));
                args.add("--patch-module");
                args.add(String.format("%s=%s", module.getName(), test.getJava().getSourceDirectories().getAsPath()));
                compileTestJava.getOptions().setCompilerArgs(args);
            }
        });
    }

    private JavaExec getRunTask(Project project) {
        Set<JavaExec> tasks = project.getTasks().withType(JavaExec.class);
        for (JavaExec task : tasks) {
            if (task.getName().equals("run")) {
                return task;
            }
        }
        throw new GradleException("No Exec task named run.");
    }

    private CreateStartScripts getStartScriptsTask(Project project) {
        Set<CreateStartScripts> tasks = project.getTasks().withType(CreateStartScripts.class);
        for (CreateStartScripts task : tasks) {
            if (task.getName().equals("startScripts")) {
                return task;
            }
        }
        throw new GradleException("No CreateStartScripts task named createStartScripts.");
    }

    private Test getTestTask(Project project) {
        Set<Test> tasks = project.getTasks().withType(Test.class);
        for (Test task : tasks) {
            if (task.getName().equals("test")) {
                return task;
            }
        }
        throw new GradleException("No Test task named test.");
    }

    private JavaCompile getCompileTestJavaTask(Project project) {
        return getJavaCompileTaskNamed(project, "compileTestJava");
    }

    private JavaCompile getCompileJavaTask(Project project) {
        return getJavaCompileTaskNamed(project, "compileJava");
    }

    private Jar getJarTask(Project project) {
        Set<Jar> tasks = project.getTasks().withType(Jar.class);
        for (Jar task : tasks) {
            if (task.getName().equals("jar")) {
                return task;
            }
        }
        throw new GradleException("No Jar task named jar.");
    }

    private JavaCompile getJavaCompileTaskNamed(Project project, String name) {
        Set<JavaCompile> tasks = project.getTasks().withType(JavaCompile.class);
        for (JavaCompile task : tasks) {
            if (task.getName().equals(name)) {
                return task;
            }
        }
        throw new GradleException(String.format("No JavaCompile task named %s.", name));

    }
}
