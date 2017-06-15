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
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JigsawPlugin implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(JigsawPlugin.class);

    private static final String JAVA_PLUGIN = "org.gradle.java";

    private static final String JAVA_LIBRARY_PLUGIN = "org.gradle.java-library";

    private static final String APPLICATION_PLUGIN = "application";

    @Override
    public void apply(Project project) {
        LOGGER.debug("Applying JigsawPlugin to " + project.getName());

        project.getExtensions().create("jigsawModule", JigsawModule.class);

        configureJavaTasks(project);
    }

    private void configureJavaTasks(Project project) {
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                if (project.getPlugins().hasPlugin(JAVA_PLUGIN) || project.getPlugins().hasPlugin(JAVA_LIBRARY_PLUGIN)) {
                    configureCompileJavaTask(project);
                } else {
                    throw new GradleException("You must apply the java or java-library plugin as well.");
                }
            }
        });
    }

    private void configureCompileJavaTask(final Project project) {
        final JavaCompile compileJava = getCompileJavaTask(project);
        final SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
        final JigsawModule module = (JigsawModule) project.getExtensions().getByName("jigsawModule");
        final String moduleDir = String.format("%s%s%s",
                main.getAllJava().getOutputDir(),
                System.getProperty("path.separator"),
                module.getName());
        compileJava.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                assert (task == compileJava);
                compileJava.setClasspath(project.files());
                List<String> args = new ArrayList<>();
                args.add("--module-path");
                args.add(project.getConfigurations().getByName("compileClasspath").getAsPath());
                args.add("--source-path");
                args.add(project.files(main.getAllJava().getSourceDirectories()).getAsPath());
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
                        copySpec.into(main.getAllJava().getOutputDir());
                    }
                });
            }
        });
    }

    private JavaCompile getCompileJavaTask(Project project) {
        Set<JavaCompile> tasks = project.getTasks().withType(JavaCompile.class);
        for (JavaCompile task : tasks) {
            if (task.getName().equals("compileJava")) {
                return task;
            }
        }
        throw new GradleException("No JavaCompile task named compileJava.");
    }
}
