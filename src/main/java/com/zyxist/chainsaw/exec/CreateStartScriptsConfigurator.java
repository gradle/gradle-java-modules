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
import com.zyxist.chainsaw.algorithms.FileRewriter;
import com.zyxist.chainsaw.algorithms.RewritingOutput;
import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.application.CreateStartScripts;

import java.io.File;
import java.io.IOException;
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
	public Optional<Action<Task>> doFirst(Project project, CreateStartScripts startScripts) {
		return Optional.of(task -> {
			JigsawCLI cli = new JigsawCLI(LIBS_PLACEHOLDER);
			cli.module(moduleConfig.getName(), startScripts.getMainClassName());
			moduleConfig.getHacks().applyHacks(cli);

			startScripts.setClasspath(project.files());
			startScripts.setDefaultJvmOpts(cli.generateArgs());
		});
	}

	@Override
	public Optional<Action<Task>> doLast(Project project, CreateStartScripts startScripts) {
		return Optional.of(task -> {
			FileRewriter bashRewriter = new FileRewriter(new File(startScripts.getOutputDir(), startScripts.getApplicationName()), FileRewriter.LineEndings.UNIX);
			FileRewriter batRewriter = new FileRewriter(new File(startScripts.getOutputDir(), startScripts.getApplicationName() + ".bat"), FileRewriter.LineEndings.WINDOWS);

			bashRewriter.rewrite(this::transformUnixFile);
			batRewriter.rewrite(this::transformWindowsFile);
		});
	}

	void transformUnixFile(String line, RewritingOutput output) throws IOException {
		if (line.contains(LIBS_PLACEHOLDER)) {
			output.emitLine(line.replaceFirst(LIBS_PLACEHOLDER, "\\$APP_HOME/lib"));
		} else {
			output.emitLine(line);
		}
	}

	void transformWindowsFile(String line, RewritingOutput output) throws IOException {
		if (line.contains(LIBS_PLACEHOLDER)) {
			output.emitLine(line.replaceFirst(LIBS_PLACEHOLDER, "%APP_HOME%\\\\\\\\lib"));
		} else {
			output.emitLine(line);
		}
	}
}
