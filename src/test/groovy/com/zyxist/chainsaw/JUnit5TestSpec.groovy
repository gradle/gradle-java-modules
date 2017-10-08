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

import com.zyxist.chainsaw.builder.Dependencies
import com.zyxist.chainsaw.builder.JigsawProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.JUnit5SampleTestFactory.junit5TestWithMocks
import static com.zyxist.chainsaw.builder.factory.RegularJavaClassFactory.regularJavaClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JUnit5TestSpec extends Specification {
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()
	JigsawProjectBuilder project

	def setup() {
		project = new JigsawProjectBuilder(tmpDir)
		project.moduleName("com.example")
			.packageName("com.example")
			.exportedPackage("com.example")
			.applyPlugin(Dependencies.JUNIT5_PLUGIN_DEPENDENCY, Dependencies.JUNIT5_PLUGIN_NAME)
			.testCompileDependency(Dependencies.JUNIT5_API_DEPENDENCY)
			.testCompileDependency(Dependencies.MOCKITO_DEPENDENCY)
			.testRuntimeDependency(Dependencies.JUNIT5_ENGINE_DEPENDENCY)
			.extraTestModule(Dependencies.MOCKITO_MODULE)
			.createJavaFile(regularJavaClass("AClass"))
			.createJavaTestFile(junit5TestWithMocks())
	}

	@IgnoreIf({NOT_JAVA_9})
	def "run JUnit5 tests with Mockito - java plugin way"() {
		given:
		project
			.gradleJavaPlugin("java")
			.createGradleBuild()
			.createModuleDescriptor()

		when:
		def result = GradleRunner.create()
				.withProjectDir(tmpDir.root)
				.forwardOutput()
				.withArguments("check")
				.withPluginClasspath().build()

		then:
		result.task(":junitPlatformTest").outcome == SUCCESS
	}

	@IgnoreIf({NOT_JAVA_9})
	def "run JUnit5 tests with Mockito - new way"() {
		given:
		project
			.gradleJavaPlugin("application")
			.createGradleBuild()
			.createModuleDescriptor()

		when:
		def result = GradleRunner.create()
				.withProjectDir(tmpDir.root)
				.forwardOutput()
				.withArguments("check")
				.withPluginClasspath().build()

		then:
		result.task(":junitPlatformTest").outcome == SUCCESS
	}
}
