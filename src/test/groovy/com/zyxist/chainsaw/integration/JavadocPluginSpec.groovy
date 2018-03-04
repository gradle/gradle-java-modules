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
import com.zyxist.chainsaw.jigsaw.JigsawFlags
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.DocumentedJavaClassFactory.documentedJavaClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JavadocPluginSpec extends Specification {
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
                .createJavaFile(documentedJavaClass())
    }

    @IgnoreIf({NOT_JAVA_9})
    def "it is possible to generate javadocs for modular project"() {
        given:
        project
                .gradleJavaPlugin("java")
                .createGradleBuild()
                .createModuleDescriptor()

        when:
        def result = GradleRunner.create()
                .withProjectDir(project.root)
                .withArguments("javadoc")
                .withPluginClasspath().build()

        then:
        result.task(":javadoc").outcome == SUCCESS
        new File(tmpDir.root, "build/tmp/javadoc/javadoc.options").exists()
        new File(tmpDir.root, "build/docs/javadoc/com/example/DocumentedClass.html").exists()
        new File(tmpDir.root, "build/tmp/javadoc/javadoc.options").text.contains(JigsawFlags.JAVADOC_MODULE_PATH)
    }
}
