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

import static com.zyxist.chainsaw.builder.factory.RunnableJavaClassFactory.runnableJavaClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class StartScriptsSpec extends Specification {
	static final NOT_IMPLEMENTED = true
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	JigsawProjectBuilder project

	def setup() {
		project = new JigsawProjectBuilder(tmpDir)
		project
			.moduleName("com.example")
			.packageName("com.example")
			.exportedPackage("com.example")
			.compileDependency(Dependencies.GUICE_DEPENDENCY)
			.gradleJavaPlugin("application")
			.mainClass("com.example.AClass")
			.createJavaFile(runnableJavaClass())
	}

	@IgnoreIf({NOT_JAVA_9})
	def "plugin adds --module-path to the generated Unix and Windows script"() {
		given:
		project
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("installDist", "--stacktrace")
			.withPluginClasspath().build()

		then:
		result.task(":installDist").outcome == SUCCESS
		new File(tmpDir.root, "build/install/modular/bin/modular").exists()
		new File(tmpDir.root, "build/install/modular/bin/modular.bat").exists()
		new File(tmpDir.root, "build/install/modular/bin/modular").text.contains("'\"--module-path\" \"\$APP_HOME/lib\"")
		new File(tmpDir.root, "build/install/modular/bin/modular.bat").text.contains("\"--module-path\" \"%APP_HOME%\\\\lib\"")
	}

	@IgnoreIf({NOT_JAVA_9})
	def "plugin adds custom --add-something flags to the generated Unix and Windows script"() {
		given:
		project
			.openedModule("com.example", "com.example", "com.google.guice")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("installDist", "--stacktrace")
			.withPluginClasspath().build()

		then:
		result.task(":installDist").outcome == SUCCESS
		new File(tmpDir.root, "build/install/modular/bin/modular").text.contains("\"--add-opens\" \"com.example/com.example=com.google.guice\"")
		new File(tmpDir.root, "build/install/modular/bin/modular.bat").text.contains("\"--add-opens\" \"com.example/com.example=com.google.guice\"")
	}

	@IgnoreIf({NOT_JAVA_9; NOT_IMPLEMENTED})
	def "plugin adds custom patch config to the generated Unix and Windows script"() {

	}

	@IgnoreIf({NOT_JAVA_9})
	def "Windows script retains line endings characteristic for that platform"() {
		given:
		project
			.openedModule("com.example", "com.example", "com.google.guice")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("installDist", "--stacktrace")
			.withPluginClasspath().build()

		then:
		new File(tmpDir.root, "build/install/modular/bin/modular.bat").text.contains("\r\n")
	}

	@IgnoreIf({NOT_JAVA_9})
	def "Unix script retains line endings characteristic for that platform"() {
		given:
		project
			.openedModule("com.example", "com.example", "com.google.guice")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.withDebug(true)
			.forwardOutput()
			.withArguments("installDist", "--stacktrace")
			.withPluginClasspath().build()

		then:
		!new File(tmpDir.root, "build/install/modular/bin/modular").text.contains("\r\n")
		new File(tmpDir.root, "build/install/modular/bin/modular").text.contains("\n")
	}
}
