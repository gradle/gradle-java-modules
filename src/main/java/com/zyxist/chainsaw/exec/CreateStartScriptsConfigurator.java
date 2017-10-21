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
package com.zyxist.chainsaw.exec;

import com.zyxist.chainsaw.JavaModule;
import com.zyxist.chainsaw.TaskConfigurator;
import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.application.CreateStartScripts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateStartScriptsConfigurator implements TaskConfigurator<CreateStartScripts> {
	private static final String LIBS_PLACEHOLDER = "APP_HOME_LIBS_PLACEHOLDER";
	private final JavaModule moduleConfig;

	public CreateStartScriptsConfigurator(JavaModule moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	@Override
	public void updateConfiguration(Project project, CreateStartScripts task) {
		task.getInputs().property("moduleName", moduleConfig.getName());
	}

	@Override
	public Action<Task> doFirst(Project project, CreateStartScripts startScripts) {
		return new Action<Task>() {
			@Override
			public void execute(Task task) {
				JigsawCLI cli = new JigsawCLI(LIBS_PLACEHOLDER);
				cli.module(moduleConfig.getName(), startScripts.getMainClassName());

				startScripts.setClasspath(project.files());
				startScripts.setDefaultJvmOpts(cli.generateArgs());
			}
		};
	}

	@Override
	public Optional<Action<Task>> doLast(Project project, CreateStartScripts startScripts) {
		return Optional.of(new Action<Task>() {
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
			for (int i = 0; i < bashContent.size(); i++) {
				bashContent.set(i, bashContent.get(i).replaceFirst(LIBS_PLACEHOLDER, newText));
			}
			Files.write(path, bashContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new GradleException("Couldn't replace placeholder in " + path);
		}
	}
}
