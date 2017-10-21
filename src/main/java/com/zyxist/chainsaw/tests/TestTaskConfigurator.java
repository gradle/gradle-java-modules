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
import com.zyxist.chainsaw.TaskConfigurator;
import com.zyxist.chainsaw.algorithms.ModulePatcher;
import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import com.zyxist.chainsaw.jigsaw.cli.PatchItem;
import com.zyxist.chainsaw.jigsaw.cli.ReadItem;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;

import java.util.Objects;

import static com.zyxist.chainsaw.ChainsawPlugin.PATCH_CONFIGURATION_NAME;

public class TestTaskConfigurator implements TaskConfigurator<Test> {
	private final JavaModule moduleConfig;
	private final TestEngine testEngine;

	public TestTaskConfigurator(JavaModule moduleConfig, TestEngine testEngine) {
		this.moduleConfig = Objects.requireNonNull(moduleConfig);
		this.testEngine = Objects.requireNonNull(testEngine);
	}

	@Override
	public void updateConfiguration(Project project, Test task) {
		task.getInputs().property("moduleName", moduleConfig.getName());
	}

	@Override
	public Action<Task> doFirst(Project project, final Test testTask) {
		return new Action<Task>() {
			@Override
			public void execute(Task task) {
				JigsawCLI cli = new JigsawCLI(testTask.getClasspath().getAsPath());
				ModulePatcher patcher = new ModulePatcher(moduleConfig.getPatchModules());
				final SourceSet test = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");

				cli.addModules()
					.addAllModulePath();
				cli.readList()
					.read(new ReadItem(moduleConfig.getName())
						.toAll(testEngine.getTestEngineModules())
						.toAll(moduleConfig.getExtraTestModules()));
				patcher
					.patchFrom(project, PATCH_CONFIGURATION_NAME)
					.forEach((k, patchedModule) -> cli.patchList().patch(patchedModule));
				cli.patchList().patch(
					new PatchItem(moduleConfig.getName())
						.with(test.getJava().getOutputDir().getAbsolutePath())
				);

				testTask.setJvmArgs(cli.generateArgs());
				testTask.setClasspath(project.files());
			}
		};
	}
}
