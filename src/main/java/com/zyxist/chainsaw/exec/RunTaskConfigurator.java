/*
 * Copyright 2017-2018 the original author or authors.
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
package com.zyxist.chainsaw.exec;

import com.zyxist.chainsaw.JavaModule;
import com.zyxist.chainsaw.TaskConfigurator;
import com.zyxist.chainsaw.algorithms.ModulePatcher;
import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import com.zyxist.chainsaw.jigsaw.cli.PatchItem;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.zyxist.chainsaw.ChainsawPlugin.PATCH_CONFIGURATION_NAME;

public class RunTaskConfigurator implements TaskConfigurator<JavaExec> {
	private final JavaModule moduleConfig;

	public RunTaskConfigurator(JavaModule moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	@Override
	public void updateConfiguration(Project project, JavaExec task) {
		task.getInputs().property("moduleName", moduleConfig.getName());
	}

	@Override
	public Optional<Action<Task>> doFirst(Project project, final JavaExec run) {
		return Optional.of(task -> {
			final SourceSet mainSourceSet = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");

			File resourceOutDir = mainSourceSet.getOutput().getResourcesDir();
			JigsawCLI cli = new JigsawCLI(stripResources(run.getClasspath().getAsPath(), resourceOutDir));
			cli.module(moduleConfig.getName(), run.getMain());
			ModulePatcher patcher = new ModulePatcher(moduleConfig.getHacks().getPatchedDependencies());
			patcher
				.patchFrom(project, PATCH_CONFIGURATION_NAME)
				.forEach((k, patchedModule) -> cli.patchList().patch(patchedModule));

			cli.patchList().patch(new PatchItem(moduleConfig.getName()).with(resourceOutDir.getAbsolutePath()));
			moduleConfig.getHacks().applyHacks(cli);

			List<String> jvmArgs = new ArrayList<>();
			jvmArgs.addAll(run.getJvmArgs());
			jvmArgs.addAll(cli.generateArgs());
			run.setJvmArgs(jvmArgs);
			run.setClasspath(project.files());
		});
	}

	private String stripResources(String classpath, File resourceOutputDir) {
		String outPath = ":" + resourceOutputDir.getAbsolutePath();
		if (classpath.contains(outPath)) {
			return classpath.replace(outPath, "");
		}
		return classpath;
	}
}
