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
package com.zyxist.chainsaw.tests;

import com.zyxist.chainsaw.JavaModule;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

import java.util.ArrayList;
import java.util.List;

public class TestEngineConfigurator {
	private static final Logger LOGGER = Logging.getLogger(TestEngineConfigurator.class);

	private final TestEngine testEngine;

	public TestEngineConfigurator(TestEngine testEngine) {
		this.testEngine = testEngine;
	}

	public Action<Task> createCompileTestJavaAction(final Project project, final SourceSet test, final JavaCompile compileTestJava, final JavaModule module) {
		return new Action<Task>() {
			@Override
			public void execute(Task task) {
				List<String> modules = merge(testEngine.getTestEngineModules(), module.getExtraTestModules());

				List<String> args = new ArrayList<>();
				args.add("--module-path");
				args.add(compileTestJava.getClasspath().getAsPath());
				createModuleConfiguration(args, module, modules);
				args.add("--patch-module");
				args.add(module.getName() + "=" + test.getJava().getSourceDirectories().getAsPath());
				compileTestJava.getOptions().setCompilerArgs(args);
				compileTestJava.setClasspath(project.files());
			}
		};
	}

	public Action<Task> createTestJavaAction(final Project project, final SourceSet test, final Test testTask, final JavaModule module) {
		return new Action<Task>() {
			@Override
			public void execute(Task task) {
				List<String> modules = merge(testEngine.getTestEngineModules(), module.getExtraTestModules());

				List<String> args = new ArrayList<>();
				args.add("--module-path");
				args.add(testTask.getClasspath().getAsPath());
				args.add("--add-modules");
				args.add("ALL-MODULE-PATH");
				createReadModuleConfiguration(args, module, modules);
				args.add("--patch-module");
				args.add(module.getName() + "=" + test.getJava().getOutputDir());
				testTask.setJvmArgs(args);
				testTask.setClasspath(project.files());
			}
		};
	}

	private static void createModuleConfiguration(List<String> args, final JavaModule module, List<String> modules) {
		args.add("--add-modules");
		args.add(joinStrings(modules));
		createReadModuleConfiguration(args, module, modules);
	}

	private static void createReadModuleConfiguration(List<String> args, final JavaModule module, List<String> testModules) {
		if (!testModules.isEmpty()) {
			args.add("--add-reads");
			StringBuilder builder = new StringBuilder();
			builder.append(module.getName()).append("=");
			boolean first = true;
			for (String extraModule: testModules) {
				if (!first) {
					builder.append(",");
				}
				builder.append(extraModule);
				first = false;
			}
			args.add(builder.toString());
		}
	}

	private static String joinStrings(List<String> items) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String item: items) {
			if (!first) {
				builder.append(',');
			}
			builder.append(item);
			first = false;
		}
		return builder.toString();
	}

	private static <T> List<T> merge(List<T> a, List<T> b) {
		List<T> result = new ArrayList<>(a.size() + b.size());
		result.addAll(a);
		result.addAll(b);
		return result;
	}
}
