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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Optional;

/**
 * Interface for reconfiguring existing Java-related tasks into Jigsaw mode.
 *
 * @param <T> Type of task to reconfigure
 */
public interface TaskConfigurator<T> {
	default void updateConfiguration(Project project, T task) {
	}

	default Optional<Action<Task>> doFirst(final Project project, final T task) {
		return Optional.empty();
	}

	default Optional<Action<Task>> doLast(final Project project, final T task) {
		return Optional.empty();
	}
}
