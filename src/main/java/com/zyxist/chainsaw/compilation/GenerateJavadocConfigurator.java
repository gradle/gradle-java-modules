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
package com.zyxist.chainsaw.compilation;

import com.zyxist.chainsaw.JavaModule;
import com.zyxist.chainsaw.TaskConfigurator;
import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import com.zyxist.chainsaw.jigsaw.JigsawFlags;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.util.Optional;

public class GenerateJavadocConfigurator implements TaskConfigurator<Javadoc> {
	private final JavaModule moduleConfig;

	public GenerateJavadocConfigurator(JavaModule moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	@Override
	public Optional<Action<Task>> doFirst(Project project, Javadoc javadoc) {
		return Optional.of(task -> {
			JigsawCLI cli = new JigsawCLI(javadoc.getClasspath().getAsPath());
			StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
			options.addStringOption(JigsawFlags.JAVADOC_MODULE_PATH, cli.getModulePath());
		});
	}
}
