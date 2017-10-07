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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.util.List;
import java.util.function.Predicate;

public interface TestEngine {

	boolean accepts(Project project);

	List<String> getTestEngineModules();

	static boolean checkTestDependencies(Project project, Predicate<Dependency> acceptor) {
		Configuration configuration = project.getConfigurations().getByName("testCompile");
		for (Dependency dependency : configuration.getDependencies()) {
			if (acceptor.test(dependency)) {
				return true;
			}
		}
		configuration = project.getConfigurations().getByName("testImplementation");
		for (Dependency dependency : configuration.getDependencies()) {
			if (acceptor.test(dependency)) {
				return true;
			}
		}
		return false;
	}
}
