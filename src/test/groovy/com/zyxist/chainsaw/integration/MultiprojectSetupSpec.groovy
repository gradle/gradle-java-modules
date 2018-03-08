/*
 * Copyright 2018 the original author or authors.
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
package com.zyxist.chainsaw.integration

import com.zyxist.chainsaw.builder.JigsawProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class MultiprojectSetupSpec extends Specification {
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()
	JigsawProjectBuilder project

	def "don't fail, if there is no /src/main/java directory due to module name autodetection"() {
		given:
		project = new JigsawProjectBuilder(tmpDir)
		project
			.moduleName("com.example")
			.gradleJavaPlugin("java")
			.dontUseExplicitModuleName()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("compileJava", '--info')
			.withPluginClasspath().build()

		then:
		result.task(":compileJava").outcome == TaskOutcome.NO_SOURCE
	}
}