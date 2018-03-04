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
package com.zyxist.chainsaw;

import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;

public enum ConfigurableTask {
	JAVA_COMPILE("java", JavaCompile.class, JavaPlugin.COMPILE_JAVA_TASK_NAME),
	JAVA_TEST_COMPILE("java", JavaCompile.class, JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME),
	JAVADOC("java", Javadoc.class, JavaPlugin.JAVADOC_TASK_NAME),
	TEST("java", Test.class, JavaPlugin.TEST_TASK_NAME),
	RUN("application", Exec.class, ApplicationPlugin.TASK_RUN_NAME),
	START_SCRIPTS("application", CreateStartScripts.class, ApplicationPlugin.TASK_START_SCRIPTS_NAME);

	private final String category;
	private final Class<?> taskType;
	private final String taskName;

	ConfigurableTask(String category, Class<?> taskType, String taskName) {
		this.taskType = taskType;
		this.taskName = taskName;
		this.category = category;
	}

	public boolean hasCategory(String category) {
		return category.equals(this.category);
	}

	public Class<?> taskType() {
		return taskType;
	}

	public String taskName() {
		return taskName;
	}
}
