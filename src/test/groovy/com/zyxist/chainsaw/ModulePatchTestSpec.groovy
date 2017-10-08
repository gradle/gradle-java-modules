package com.zyxist.chainsaw

import com.zyxist.chainsaw.builder.Dependencies
import com.zyxist.chainsaw.builder.JigsawProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.zyxist.chainsaw.builder.factory.JUnit4SampleTestFactory.junit4Test
import static com.zyxist.chainsaw.builder.factory.RegularJavaClassFactory.regularJavaClass
import static com.zyxist.chainsaw.builder.factory.RunnableGuavaClassFactory.runnableGuavaJsr250Class
import static com.zyxist.chainsaw.builder.factory.RunnableJavaClassFactory.runnableJavaClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ModulePatchTestSpec extends Specification {
	static final NOT_JAVA_9 = !System.getProperty("java.version").startsWith("9")
	@Rule
	final TemporaryFolder tmpDir = new TemporaryFolder()

	JigsawProjectBuilder project

	def setup() {
		project = new JigsawProjectBuilder(tmpDir)
		project.moduleName("com.example")
			.packageName("com.example")
			.requiredModule(Dependencies.JSR250_MODULE)
			.requiredModule(Dependencies.GUAVA_MODULE)
			.exportedPackage("com.example")
			.patchDependency(Dependencies.JSR305_DEPENDENCY)
			.patchedModule(Dependencies.JSR305_PATCH, Dependencies.JSR250_PATCH)
			.compileDependency(Dependencies.JSR250_DEPENDENCY)
			.compileDependency(Dependencies.GUAVA_DEPENDENCY)
			.createJavaFile(regularJavaClass("SomeClass"))
	}

	@IgnoreIf({NOT_JAVA_9})
	def "patch rouge jsr305 during compilation - java plugin"() {
		given:
		project
			.gradleJavaPlugin("java")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.forwardOutput()
			.withArguments("assemble")
			.withPluginClasspath().build()

		then:
		result.task(":compileJava").outcome == SUCCESS
		result.task(":jar").outcome == SUCCESS
		new File(tmpDir.root, "build/libs/modular.jar").exists()
		new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/SomeClass.class").exists()
	}

	@IgnoreIf({NOT_JAVA_9})
	def "patch rouge jsr305 during compilation - library plugin"() {
		given:
		project
			.gradleJavaPlugin("java-library")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.forwardOutput()
			.withArguments("assemble")
			.withPluginClasspath().build()

		then:
		result.task(":compileJava").outcome == SUCCESS
		result.task(":jar").outcome == SUCCESS
		new File(tmpDir.root, "build/libs/modular.jar").exists()
		new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/SomeClass.class").exists()
	}

	@IgnoreIf({NOT_JAVA_9})
	def "patch rouge jsr305 during compilation - application plugin"() {
		given:
		project
			.gradleJavaPlugin("application")
			.createJavaFile(runnableJavaClass())
			.mainClass("com.example.AClass")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.forwardOutput()
			.withArguments("assemble")
			.withPluginClasspath().build()

		then:
		result.task(":compileJava").outcome == SUCCESS
		result.task(":jar").outcome == SUCCESS
		new File(tmpDir.root, "build/libs/modular.jar").exists()
		new File(tmpDir.root, "build/classes/java/main/module-info.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/SomeClass.class").exists()
		new File(tmpDir.root, "build/classes/java/main/com/example/AClass.class").exists()
	}

	@IgnoreIf({NOT_JAVA_9})
	def "patch rogue jsr305 in unit tests - application plugin"() {
		given:
		project
			.testCompileDependency(Dependencies.JUNIT4_DEPENDENCY)
			.gradleJavaPlugin("application")
			.createJavaFile(runnableGuavaJsr250Class())
			.createJavaTestFile(junit4Test())
			.mainClass("com.example.AClass")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.forwardOutput()
			.withArguments("check")
			.withPluginClasspath().build()

		then:
		result.task(":test").outcome == SUCCESS
	}

	@IgnoreIf({NOT_JAVA_9})
	def "patch rogue jsr305 in runtime"() {
		given:
		project
			.gradleJavaPlugin("application")
			.createJavaFile(runnableGuavaJsr250Class())
			.mainClass("com.example.AClass")
			.createModuleDescriptor()
			.createGradleBuild()

		when:
		def result = GradleRunner.create()
			.withProjectDir(project.root)
			.forwardOutput()
			.withArguments("run")
			.withPluginClasspath()
			.build()

		then:
		result.output.contains("Hello World!")
	}
}
