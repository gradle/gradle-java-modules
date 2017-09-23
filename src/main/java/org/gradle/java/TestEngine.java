package org.gradle.java;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

public interface TestEngine {

    boolean accepts(Project project);

    Action<Task> createCompileTestJavaAction(final Project project, final SourceSet test, JavaCompile compileTestJava, JavaModule module);

    Action<Task> createTestJavaAction(final Project project, final SourceSet test, Test compileTestJava, JavaModule module);
}
