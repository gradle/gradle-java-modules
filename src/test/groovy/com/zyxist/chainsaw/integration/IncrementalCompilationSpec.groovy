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
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.JUnit4SampleTestFactory.junit4Test
import static com.zyxist.chainsaw.builder.factory.RegularJavaClassFactory.regularJavaClass
import static com.zyxist.chainsaw.builder.factory.RunnableJavaClassFactory.runnableJavaClass
import static org.gradle.testkit.runner.TaskOutcome.*

class IncrementalCompilationSpec extends Specification {
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

	JigsawProjectBuilder project

	def setup() {
		project = new JigsawProjectBuilder(tmpDir)
		project
			.enableIncrementalCompilation()
			.moduleName("com.example")
			.packageName("com.example")
			.exportedPackage("com.example")
			.gradleJavaPlugin("application")
			.testCompileDependency(Dependencies.JUNIT4_DEPENDENCY)

	}


	def "the plugin does not break incremental compilation in compileJava task, when one of classes is changed"() {
		given:
		project
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
			.createJavaFile(regularJavaClass("Foo"))
			.createJavaTestFile(junit4Test())
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def firstRun = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("assemble")
			.withPluginClasspath().build()
		project.replaceJavaFile(runnableJavaClass("Changed text."))
		def secondRun = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("assemble", "--info")
			.withPluginClasspath().build()

		then:
		firstRun.task(":compileJava").outcome == SUCCESS
		secondRun.task(":compileJava").outcome == SUCCESS
		secondRun.output.contains("Incremental compilation of 1 classes completed")
		new File(tmpDir.root, "build/libs/modular.jar").exists()
		new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/AClass.class").exists()
	}

	def "the plugin does not break incremental compilation in compileJava task, when there are no changes"() {
		given:
		project
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
			.createJavaFile(regularJavaClass("Foo"))
			.createJavaTestFile(junit4Test())
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def firstRun = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("assemble")
			.withPluginClasspath().build()
		def secondRun = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("assemble", "--info")
			.withPluginClasspath().build()

		then:
		firstRun.task(":compileJava").outcome == SUCCESS
		secondRun.task(":compileJava").outcome == UP_TO_DATE
		new File(tmpDir.root, "build/libs/modular.jar").exists()
		new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/AClass.class").exists()
	}
}
