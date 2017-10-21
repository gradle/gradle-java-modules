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
package com.zyxist.chainsaw;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Objects;

/**
 * Orchestrates the re-configuration of various Java tasks into Jigsaw mode. The supported tasks
 * are specified by {@link ConfigurableTask} enumerator.
 */
public class TaskConfigurationOrchestrator {
	private final EnumMap<ConfigurableTask, TaskConfigurator> configurators = new EnumMap<>(ConfigurableTask.class);

	public TaskConfigurationOrchestrator use(ConfigurableTask type, TaskConfigurator configurator) {
		this.configurators.put(type, Objects.requireNonNull(configurator));
		return this;
	}

	public Collection<TaskConfigurator> getConfigurators() {
		return this.configurators.values();
	}

	public void configureTasks(final Project project, String category) {
		configurators.forEach((type, configurator) -> {
			if (type.hasCategory(category)) {
				configureTask(project, type, configurator);
			}
		});
	}

	private void configureTask(Project project, ConfigurableTask taskType, TaskConfigurator configurator) {
		final Task task = project.getTasks().findByName(taskType.taskName());
		configurator.updateConfiguration(project, task);
		task.doFirst(configurator.doFirst(project, task));
		configurator
			.doLast(project, task)
			.ifPresent(action -> task.doLast((Action<Task>)action));
	}
}
