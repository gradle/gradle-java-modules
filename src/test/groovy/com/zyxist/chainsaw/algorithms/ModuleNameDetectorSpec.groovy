package com.zyxist.chainsaw.algorithms

import com.github.javaparser.ParserConfiguration
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ModuleNameDetectorSpec extends Specification {
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	def "it should be able to parse a Java module descriptor and extract the name"() {
		given:
		tmpDir.newFile("module-info.java").text = """
module com.example.foo {
}
"""

		ModuleNameDetector detector = new ModuleNameDetector(JavaVersion.VERSION_1_9)

		when:
		def result = detector.findModuleName(tmpDir.root)

		then:
		result == 'com.example.foo'
	}

	def "it should report a missing module descriptor"() {
		given:
		ModuleNameDetector detector = new ModuleNameDetector(JavaVersion.VERSION_1_9)

		when:
		detector.findModuleName(tmpDir.root)

		then:
		GradleException ex = thrown()
		ex.getMessage() == 'The project is lacking a Java module descriptor in \'' + tmpDir.root.getPath() + '\' directory.'
	}

	def "it recognizes Java 9"() {
		given:
		ModuleNameDetector detector = new ModuleNameDetector(JavaVersion.VERSION_1_9)

		when:
		def result = detector.getLanguageLevel()

		then:
		result == ParserConfiguration.LanguageLevel.JAVA_9
	}

	def "it recognizes Java 10"() {
		given:
		ModuleNameDetector detector = new ModuleNameDetector(JavaVersion.VERSION_1_10)

		when:
		def result = detector.getLanguageLevel()

		then:
		result == ParserConfiguration.LanguageLevel.JAVA_10
	}

	def "other (future) versions of Java default to Java 9"() {
		given:
		// using Java 8, because there is nothing newer than 10 in Gradle API
		ModuleNameDetector detector = new ModuleNameDetector(JavaVersion.VERSION_1_8)

		when:
		def result = detector.getLanguageLevel()

		then:
		result == ParserConfiguration.LanguageLevel.JAVA_9
	}
}
