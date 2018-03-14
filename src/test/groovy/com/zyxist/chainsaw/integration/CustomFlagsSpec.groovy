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

import com.zyxist.chainsaw.builder.Dependencies
import com.zyxist.chainsaw.builder.JigsawProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.JUnit4SampleTestFactory.junit4Test
import static com.zyxist.chainsaw.builder.factory.RunnableHackingClassFactory.runnableHackingJavaClass
import static com.zyxist.chainsaw.builder.factory.RunnableJavaClassFactory.runnableJavaClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CustomFlagsSpec extends Specification {
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

	JigsawProjectBuilder project

	def setup() {
		project = new JigsawProjectBuilder(tmpDir)
		project
			.moduleName("com.example")
			.packageName("com.example")
			.exportedPackage("com.example")
			.gradleJavaPlugin("application")

	}

	@IgnoreIf({NOT_JAVA_9})
	def "it is possible to apply custom --add-opens flags to the runner"() {
		given:
		project
			.mainClass("com.example.AClass")
			.createJavaFile(runnableHackingJavaClass())
			.openedModule("java.base", "java.lang", "com.example")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("run", "--info", "--stacktrace")
			.withPluginClasspath().build()

		then:
		result.task(":run").outcome == SUCCESS
		result.output.contains("--module-path")
		result.output.contains("--add-opens java.base/java.lang=com.example")
		result.output.contains("Success - old cglib will work")
	}

	@IgnoreIf({NOT_JAVA_9})
	def "it is possible to apply compiler arguments"() {
		given:
		project
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
			.withCompilerArgs("-proc:none")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("assemble", "--debug")
			.withPluginClasspath().build()

		then:
		result.task(":compileJava").outcome == SUCCESS
		result.task(":jar").outcome == SUCCESS
		result.output.contains("--module-path")
		result.output.contains("-proc:none")
		new File(tmpDir.root, "build/libs/modular.jar").exists()
	}

	@IgnoreIf({NOT_JAVA_9})
	def "it is possible to apply custom JVM arguments"() {
		given:
		project
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
			.withJvmArgs("-XX:NewSize=64M")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("run", "--debug")
			.withPluginClasspath().build()

		then:
		result.task(":run").outcome == SUCCESS
		result.output.contains("--module-path")
		result.output.contains("-XX:NewSize=64M")
	}

	@IgnoreIf({NOT_JAVA_9})
	def "it is possible to apply custom JVM arguments for tests"() {
		given:
		project
			.testCompileDependency(Dependencies.JUNIT4_DEPENDENCY)
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
			.createJavaTestFile(junit4Test())
			.withTestJvmArgs("-XX:NewSize=128M")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("check", "--debug")
			.withPluginClasspath().build()

		then:
		result.task(":test").outcome == SUCCESS
		result.output.contains("--module-path")
		result.output.contains("-XX:NewSize=128M")
	}
}
