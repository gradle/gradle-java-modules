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

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class VerifyModuleNameTaskSpec extends Specification {
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	def configureProjectWithModule(String moduleName) {
		def buildFile = tmpDir.newFile("build.gradle")
		buildFile << """
plugins {
  id 'application'
  id 'com.zyxist.chainsaw' version '0.1.2'
}

repositories {
  jcenter()
}

dependencies {
  testImplementation 'junit:junit:4.12'
}

javaModule.name = '${moduleName}'
mainClassName = 'com.example.AClass'
"""
		def settingsFile = tmpDir.newFile("settings.gradle")
		settingsFile << """
rootProject.name = "modular"
"""
		tmpDir.newFolder("src", "main", "java", "com", "example")
		tmpDir.newFolder("src", "test", "java", "com", "example")

		def moduleDescriptor = tmpDir.newFile("src/main/java/module-info.java")
		moduleDescriptor << """
module ${moduleName} {
  exports com.example;
}
"""
		def sourceFile = tmpDir.newFile("src/main/java/com/example/AClass.java")
		sourceFile << """
package com.example;
public class AClass {
  public void aMethod(String aString) {
    System.out.println(aString);
  }
  
  public static void main(String... args) {
    new AClass().aMethod("Hello World!");
  }
}
"""
	}

	def "succeeds for module named from the root package"() {
		given:
		configureProjectWithModule('com.example')

		when:
		def result = GradleRunner.create()
				.withProjectDir(tmpDir.root)
				.withArguments("verifyModuleName")
				.withPluginClasspath().build()

		then:
		result.task(":verifyModuleName").outcome == SUCCESS
	}

	def "fails for module with custom naming"() {
		given:
		configureProjectWithModule('test.module')

		when:
		def result = GradleRunner.create()
				.withProjectDir(tmpDir.root)
				.withArguments("verifyModuleName")
				.withPluginClasspath()
				.buildAndFail()

		then:
		result.task(":verifyModuleName").outcome == FAILED
	}
}
