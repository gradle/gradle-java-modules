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
package com.zyxist.chainsaw.integration

import com.zyxist.chainsaw.builder.Dependencies
import com.zyxist.chainsaw.builder.JigsawProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.DaggerComponentFactory.daggerComponent
import static com.zyxist.chainsaw.builder.factory.DaggerModuleFactory.daggerModule
import static com.zyxist.chainsaw.builder.factory.RegularJavaClassFactory.regularJavaClass
import static com.zyxist.chainsaw.builder.factory.RunnableDaggerClassFactory.runnableDaggerClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AnnotationProcessingTestSpec extends Specification {
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	JigsawProjectBuilder project

	def setup() {
		project = new JigsawProjectBuilder(tmpDir)
		project.moduleName("com.example")
			.packageName("com.example")
			.useAnnotationProcessor()
			.gradleJavaPlugin("application")
			.requiredModule(Dependencies.JSR250_MODULE)
			.requiredModule(Dependencies.DAGGER_MODULE)
			.exportedPackage("com.example")
			.patchDependency(Dependencies.JSR305_DEPENDENCY)
			.patchedModule(Dependencies.JSR305_PATCH, Dependencies.JSR250_PATCH)
			.compileDependency(Dependencies.JSR250_DEPENDENCY)
			.compileDependency(Dependencies.DAGGER_DEPENDENCY)
			.aptDependency(Dependencies.JSR250_DEPENDENCY)
			.aptDependency(Dependencies.DAGGER_COMPILER_DEPENDENCY)
			.createJavaFile(regularJavaClass("SomeClass"))
			.createJavaFile(daggerComponent())
			.createJavaFile(daggerModule())
			.createJavaFile(runnableDaggerClass())
			.mainClass("com.example.AClass")
	}

	@IgnoreIf({NOT_JAVA_9})
	def "chainsaw is able to build projects with annotation processors: Dagger scenario"() {
		given:
		project
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("--stacktrace", "assemble")
			.withPluginClasspath().build()

		then:
		result.task(":compileJava").outcome == SUCCESS
		result.task(":jar").outcome == SUCCESS
		new File(tmpDir.root, "build/libs/modular.jar").exists()
		new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/SomeClass.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/AClass.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/DaggerApplicationComponent.class").exists()
	}

	@IgnoreIf({NOT_JAVA_9})
	def "chainsaw is able to run projects with annotation processors: Dagger scenario"() {
		given:
		project
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("--stacktrace", "run")
			.withPluginClasspath()
			.build()

		then:
		result.output.contains("Hello World!")
	}
}
