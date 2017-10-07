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

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JUnit5TestSpec extends Specification {
	final static MOCKITO_MODULE = 'mockito.core'
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	def setup() {
		def settingsFile = tmpDir.newFile("settings.gradle")
		settingsFile << """
rootProject.name = "modular"
"""
		tmpDir.newFolder("src", "main", "java", "com", "example")
		tmpDir.newFolder("src", "test", "java", "com", "example")

		def moduleDescriptor = tmpDir.newFile("src/main/java/module-info.java")
		moduleDescriptor << """
module com.example {
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
		def testFile = tmpDir.newFile("src/test/java/com/example/AClassTest.java")
		testFile << """
package com.example;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AClassTest {
  @Test
  public void isAnInstanceOfAClass() {
      Object obj = mock(Object.class);
      assertTrue(new AImplementation() instanceof AClass);
  }
  
  static class AImplementation extends AClass {
    @Override
    public void aMethod(String aString) {
        // Do nothing
    }
  }
}
"""
	}

	@IgnoreIf({NOT_JAVA_9})
	def "run JUnit5 tests with Mockito - java plugin way"() {
		given:
		def buildFile = tmpDir.newFile("build.gradle")
		buildFile << """
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath 'org.junit.platform:junit-platform-gradle-plugin:1.0.0'
    }
}

plugins {
  id 'java'
  id 'com.zyxist.chainsaw' version '0.1.2'
}

apply plugin: 'org.junit.platform.gradle.plugin'

repositories {
  mavenLocal()
  jcenter()
}

dependencies {
  testCompile 'org.mockito:mockito-core:2.10.0'
  testCompile 'org.junit.jupiter:junit-jupiter-api:5.0.0'
  testRuntime 'org.junit.jupiter:junit-jupiter-engine:5.0.0'
}

javaModule.name = 'com.example'
javaModule.extraTestModules = ['${MOCKITO_MODULE}']
"""

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
		def buildFile = tmpDir.newFile("build.gradle")
		buildFile << """
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath 'org.junit.platform:junit-platform-gradle-plugin:1.0.0'
    }
}

plugins {
  id 'application'
  id 'com.zyxist.chainsaw' version '0.1.2'
}

apply plugin: 'org.junit.platform.gradle.plugin'

repositories {
  mavenLocal()
  jcenter()
}

dependencies {
  testImplementation 'org.mockito:mockito-core:2.10.0'
  testImplementation 'org.junit.jupiter:junit-jupiter-api:5.0.0'
  testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.0.0'
}

javaModule.name = 'com.example'
javaModule.extraTestModules = ['${MOCKITO_MODULE}']
"""

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
