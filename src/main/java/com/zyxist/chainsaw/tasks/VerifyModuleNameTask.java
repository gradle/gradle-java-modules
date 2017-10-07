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
package com.zyxist.chainsaw.tasks;

import com.zyxist.chainsaw.JavaModule;
import com.zyxist.chainsaw.algorithms.ModuleNameChecker;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class VerifyModuleNameTask extends DefaultTask {
	private static final String ERROR_TEMPLATE = "The module name '%s' does not follow the official " +
		"module naming convention for Java (reverse-DNS style, derived from the root package).";

	@TaskAction
	public void verifyModuleName() {
		Project project = getProject();
		SourceSet mainSourceSet = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().findByName("main");
		JavaModule javaModule = project.getExtensions().getByType(JavaModule.class);

		for (File srcDir: mainSourceSet.getAllJava().getSourceDirectories()) {
			ModuleNameChecker checker = new ModuleNameChecker(srcDir.getAbsolutePath());

			if (!checker.verifyModuleName(javaModule.getName())) {
				throw new GradleException(String.format(ERROR_TEMPLATE, javaModule.getName()));
			}
		}
	}
}
