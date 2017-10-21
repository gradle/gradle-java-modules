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
package com.zyxist.chainsaw

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.mockito.ArgumentMatchers
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class TaskConfigurationOrchestratorSpec extends Specification {

	def "should register configurators with use() method"() {
		given:
		def orchestrator = new TaskConfigurationOrchestrator()
		def firstTask = mock(TaskConfigurator.class)
		def secondTask = mock(TaskConfigurator.class)

		when:
		orchestrator.use(ConfigurableTask.JAVA_COMPILE, firstTask)
		orchestrator.use(ConfigurableTask.JAVA_TEST_COMPILE, secondTask)

		then:
		orchestrator.getConfigurators().size() == 2
		orchestrator.getConfigurators().contains(firstTask)
		orchestrator.getConfigurators().contains(secondTask)
	}

	def "should configure all tasks from a single category"() {
		given:
		def orchestrator = new TaskConfigurationOrchestrator()

		def firstTask = mock(Task.class)
		def secondTask = mock(Task.class)

		def firstConfigurator = mock(TaskConfigurator.class)
		def secondConfigurator = mock(TaskConfigurator.class)
		def thirdConfigurator = mock(TaskConfigurator.class)

		def taskContainer = mock(TaskContainer.class)
		def project = mock(Project.class)
		when(project.getTasks()).thenReturn(taskContainer)
		when(taskContainer.findByName(ConfigurableTask.JAVA_COMPILE.taskName())).thenReturn(firstTask)
		when(taskContainer.findByName(ConfigurableTask.JAVA_TEST_COMPILE.taskName())).thenReturn(secondTask)

		orchestrator.use(ConfigurableTask.JAVA_COMPILE, firstConfigurator)
			.use(ConfigurableTask.JAVA_TEST_COMPILE, secondConfigurator)
			.use(ConfigurableTask.RUN, thirdConfigurator)

		when:
		orchestrator.configureTasks(project, "java")

		then:
		verify(firstConfigurator).doFirst(project, firstTask) || true
		verify(secondConfigurator).doFirst(project, secondTask) || true
		verify(thirdConfigurator, never()).doFirst(eq(project), ArgumentMatchers.any(Task.class)) || true
	}

}
