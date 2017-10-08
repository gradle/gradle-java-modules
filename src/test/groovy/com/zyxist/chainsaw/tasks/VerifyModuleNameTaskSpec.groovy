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
package com.zyxist.chainsaw.tasks

import com.zyxist.chainsaw.builder.JigsawProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.RunnableJavaClassFactory.runnableJavaClass
import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class VerifyModuleNameTaskSpec extends Specification {

	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	JigsawProjectBuilder project

	def configureProjectWithModule(String moduleName) {
		project = new JigsawProjectBuilder(tmpDir)
		project
			.moduleName(moduleName)
			.packageName("com.example")
			.exportedPackage("com.example")
			.gradleJavaPlugin("application")
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
			.createModuleDescriptor()
			.createGradleBuild()
	}

	@IgnoreIf({NOT_JAVA_9})
	def "succeeds for module named from the root package"() {
		given:
		configureProjectWithModule('com.example')

		when:
		def result = GradleRunner.create()
				.withProjectDir(project.root)
				.withArguments("verifyModuleName")
				.withPluginClasspath().build()

		then:
		result.task(":verifyModuleName").outcome == SUCCESS
	}

	@IgnoreIf({NOT_JAVA_9})
	def "fails for module with custom naming"() {
		given:
		configureProjectWithModule('test.module')

		when:
		def result = GradleRunner.create()
				.withProjectDir(project.root)
				.withArguments("verifyModuleName")
				.withPluginClasspath()
				.buildAndFail()

		then:
		result.task(":verifyModuleName").outcome == FAILED
	}
}
