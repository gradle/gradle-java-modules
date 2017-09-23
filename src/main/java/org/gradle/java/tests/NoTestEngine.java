package org.gradle.java.tests;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.java.JavaModule;
import org.gradle.java.TestEngine;

public class NoTestEngine implements TestEngine {

    @Override
    public boolean accepts(Project project) {
        return true;
    }

    @Override
    public Action<Task> createCompileTestJavaAction(Project project, SourceSet test, JavaCompile compileTestJava, JavaModule module) {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
            }
        };
    }

    @Override
    public Action<Task> createTestJavaAction(Project project, SourceSet test, Test compileTestJava, JavaModule module) {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
            }
        };
    }
}
