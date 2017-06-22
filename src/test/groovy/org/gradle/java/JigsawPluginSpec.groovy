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
package org.gradle.java

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JigsawPluginSpec extends Specification {
    @Rule
    final TemporaryFolder tmpDir = new TemporaryFolder()
    static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")

    def setup() {
        def buildFile = tmpDir.newFile("build.gradle")
        buildFile << """
plugins {
  id 'application'
  id 'org.gradle.java.experimental-jigsaw' version '0.1.0'
}

repositories {
  jcenter()
}

dependencies {
  testImplementation 'junit:junit:4.12'
}

javaModule.name = 'test.module'
mainClassName = 'io.example.AClass'
"""
        def settingsFile = tmpDir.newFile("settings.gradle")
        settingsFile << """
rootProject.name = "modular"
"""
        tmpDir.newFolder("src", "main", "java", "io", "example")
        tmpDir.newFolder("src", "test", "java", "io", "example")

        def moduleDescriptor = tmpDir.newFile("src/main/java/module-info.java")
        moduleDescriptor << """
module test.module {
  exports io.example;
}
"""
        def sourceFile = tmpDir.newFile("src/main/java/io/example/AClass.java")
        sourceFile << """
package io.example;
public class AClass {
  public void aMethod(String aString) {
    System.out.println(aString);
  }
  
  public static void main(String... args) {
    new AClass().aMethod("Hello World!");
  }
}
"""
        def testFile = tmpDir.newFile("src/test/java/io/example/AClassTest.java")
        testFile << """
package io.example;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class AClassTest {
  @Test
  public void isAnInstanceOfAClass() {
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
    def "can assemble a module"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(tmpDir.root)
                .withArguments("assemble")
                .withPluginClasspath().build()

        then:
        result.task(":compileJava").outcome == SUCCESS
        result.task(":jar").outcome == SUCCESS
        new File(tmpDir.root, "build/libs/modular.jar").exists()
        new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
        new File(tmpDir.root, "build/classes/java/main/io/example/AClass.class").exists()
    }

    @IgnoreIf({NOT_JAVA_9})
    def "can check a module"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(tmpDir.root)
                .withArguments("check")
                .withPluginClasspath().build()

        then:
        result.task(":test").outcome == SUCCESS
    }

    @IgnoreIf({NOT_JAVA_9})
    def "can run with a module"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(tmpDir.root)
                .withArguments("run")
                .withPluginClasspath().build()

        then:
        result.output.contains("Hello World!")
    }
}