/*
 * Copyright 2017-2018 the original author or authors.
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
package com.zyxist.chainsaw.builder;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JigsawProjectBuilder {
	private static final String PLUGIN_VERSION = "0.2.0";

	private static final String DEFAULT_PKG_NAME = "com.example";
	private static final String DEFAULT_MODULE_NAME = "com.example";

	private static final String JAVA_SRC = "src/main/java";
	private static final String TEST_SRC = "src/test/java";

	private String javaPlugin = "";
	private String pkgName = DEFAULT_PKG_NAME;
	private String moduleName = DEFAULT_MODULE_NAME;
	private String mainClassName = null;
	private boolean useApt = false;
	private boolean useJavadoc = false;
	private boolean allowNameViolations = false;

	private final List<String> applyPluginClasspath = new ArrayList<>();
	private final List<String> appliedPlugins = new ArrayList<>();

	private final List<String> requiredModules = new ArrayList<>();
	private final List<String> exportedPackages = new ArrayList<>();

	private final List<String> extraTestModules = new ArrayList<>();
	private final List<String> patchConfig = new ArrayList<>();

	private final List<String> patchDependencies = new ArrayList<>();
	private final List<String> compileDependencies = new ArrayList<>();
	private final List<String> runtimeDependencies = new ArrayList<>();
	private final List<String> testCompileDependencies = new ArrayList<>();
	private final List<String> testRuntimeDependencies = new ArrayList<>();
	private final List<String> aptDependencies = new ArrayList<>();

	final TemporaryFolder tmpDir;

	public JigsawProjectBuilder(TemporaryFolder folder) {
		this.tmpDir = folder;
	}

	public JigsawProjectBuilder packageName(String name) {
		this.pkgName = name;
		return this;
	}

	public JigsawProjectBuilder moduleName(String name) {
		this.moduleName = name;
		return this;
	}

	public JigsawProjectBuilder mainClass(String name) {
		this.mainClassName = name;
		return this;
	}

	public JigsawProjectBuilder allowNameViolations(boolean value) {
		this.allowNameViolations = value;
		return this;
	}

	public JigsawProjectBuilder gradleJavaPlugin(String javaPlugin) {
		this.javaPlugin = javaPlugin;
		return this;
	}

	public JigsawProjectBuilder applyPlugin(String classpath, String descriptor) {
		this.applyPluginClasspath.add(classpath);
		this.appliedPlugins.add(descriptor);
		return this;
	}

	public JigsawProjectBuilder useAnnotationProcessor() {
		this.useApt = true;
		return this;
	}

	public JigsawProjectBuilder useJavadoc() {
		this.useJavadoc = true;
		return this;
	}

	public JigsawProjectBuilder requiredModule(String module) {
		this.requiredModules.add(module);
		return this;
	}

	public JigsawProjectBuilder exportedPackage(String pkg) {
		this.exportedPackages.add(pkg);
		return this;
	}

	public JigsawProjectBuilder extraTestModule(String moduleName) {
		this.extraTestModules.add(moduleName);
		return this;
	}

	public JigsawProjectBuilder patchedModule(String patch, String patched) {
		this.patchConfig.add("'" + patch + "': '" + patched+"'");
		return this;
	}

	public JigsawProjectBuilder patchDependency(String dep) {
		this.patchDependencies.add(dep);
		return this;
	}

	public JigsawProjectBuilder compileDependency(String dep) {
		this.compileDependencies.add(dep);
		return this;
	}

	public JigsawProjectBuilder runtimeDependency(String dep) {
		this.runtimeDependencies.add(dep);
		return this;
	}

	public JigsawProjectBuilder testCompileDependency(String dep) {
		this.testCompileDependencies.add(dep);
		return this;
	}

	public JigsawProjectBuilder testRuntimeDependency(String dep) {
		this.testRuntimeDependencies.add(dep);
		return this;
	}

	public JigsawProjectBuilder aptDependency(String dep) {
		this.aptDependencies.add(dep);
		return this;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getPackageName() {
		return pkgName;
	}

	public JigsawProjectBuilder createModuleDescriptor() throws IOException {
		StringBuilder descriptor = new StringBuilder("module " + this.moduleName + " {\n");
		for (String module: requiredModules) {
			descriptor.append("    requires ").append(module).append(";\n");
		}
		for (String pkg: exportedPackages) {
			descriptor.append("    exports ").append(pkg).append(";\n");
		}
		descriptor.append("}\n");
		File file = tmpDir.newFile(JAVA_SRC + "/module-info.java");
		ResourceGroovyMethods.leftShift(file, descriptor.toString());
		return this;
	}

	public JigsawProjectBuilder createJavaFile(JavaCodeFactory factory) throws IOException {
		String path = JAVA_SRC + "/" + factory.getFilename(this);
		createDirectories(path);
		File file = tmpDir.newFile(path);
		ResourceGroovyMethods.leftShift(file, factory.generateCode(this));
		return this;
	}

	public JigsawProjectBuilder createJavaTestFile(JavaCodeFactory factory) throws IOException {
		String path = TEST_SRC + "/" + factory.getFilename(this);
		createDirectories(path);
		File file = tmpDir.newFile(path);
		ResourceGroovyMethods.leftShift(file, factory.generateCode(this));
		return this;
	}

	public JigsawProjectBuilder createGradleBuild() throws IOException {
		StringBuilder build = new StringBuilder();

		if (!applyPluginClasspath.isEmpty()) {
			generateBuildscript(build);
		}

		generatePluginSection(build);
		generateApplyPluginSection(build);

		if (useApt) {
			generateAnnotationProcessingSourceSets(build);
		}
		generateRepositories(build);
		generateDependencies(build);
		generateApplicationConfig(build);
		generateJavaModuleConfig(build);
		if (useJavadoc) {
			generateJavadocConfig(build);
		}

		File file = tmpDir.newFile("build.gradle");
		ResourceGroovyMethods.leftShift(file, build.toString());

		File settingsfile = tmpDir.newFile("settings.gradle");
		ResourceGroovyMethods.leftShift(settingsfile, "rootProject.name = 'modular'\n");

		return this;
	}

	private void generateApplicationConfig(StringBuilder build) {
		if (null != mainClassName) {
			build.append("mainClassName = '"+mainClassName+"'\n");
		}
	}

	private void generateJavaModuleConfig(StringBuilder build) {
		build.append("javaModule.name = '" + moduleName+"'\n");
		if (allowNameViolations) {
			build.append("javaModule.allowModuleNamingViolations = true\n");
		}
		if (!extraTestModules.isEmpty()) {
			build.append("javaModule.extraTestModules = ['" + String.join("', '", extraTestModules) + "']\n");
		}
		if (!patchConfig.isEmpty()) {
			build.append("javaModule.patchModules " + String.join(",\n", patchConfig) + "\n");
		}
	}

	private void generateDependencies(StringBuilder build) {
		build.append("dependencies {\n");
		if (javaPlugin.equals("java")) {
			generateJavaPluginDependencies(build);
		} else if (javaPlugin.equals("application")) {
			generateApplicationPluginDependencies(build);
		} else {
			generateLibraryPluginDependencies(build);
		}

		build.append("}\n");
	}

	private void generateRepositories(StringBuilder build) {
		build.append("repositories {\n");
		build.append("	jcenter()\n");
		build.append("}\n");
	}

	private void generatePluginSection(StringBuilder build) {
		build.append("plugins {\n");
		build.append("	id '"+javaPlugin+"'\n");
		if (useApt) {
			build.append("	id 'net.ltgt.apt' version '"+Dependencies.APT_PLUGIN_VERSION+"'\n");
		}
		build.append("	id 'com.zyxist.chainsaw'\n");
		build.append("}\n");
	}

	private void generateAnnotationProcessingSourceSets(StringBuilder build) {
		build.append("sourceSets {\n");
		build.append("	main {\n");
		build.append("		java {\n");
		build.append("			srcDirs = ['src/main/java', 'build/generated/source/apt/main']\n");
		build.append("		}\n");
		build.append("	}\n");
		build.append("}\n\n");
	}

	private void generateJavadocConfig(StringBuilder build) {
		build.append("javadoc {");
		build.append("	source = sourceSets.main.allJava");
		build.append("}");
	}

	private void generateBuildscript(StringBuilder build) {
		build.append("buildscript {\n");
		build.append("	repositories {\n");
		build.append("		mavenLocal()\n");
		build.append("		jcenter()\n");
		build.append("	}\n");
		build.append("	dependencies {\n");
		for (String cp: applyPluginClasspath) {
			build.append("		classpath '"+cp+"'\n");
		}
		build.append("	}\n");
		build.append("}\n\n");
	}

	private void generateApplyPluginSection(StringBuilder build) {
		for (String plugin: appliedPlugins) {
			build.append("apply plugin: '"+plugin+"'\n");
		}
	}

	private void generateLibraryPluginDependencies(StringBuilder build) {
		for (String dep: patchDependencies) {
			build.append("	patch '"+dep+"'\n");
		}
		for (String dep: compileDependencies) {
			build.append("	api '"+dep+"'\n");
		}
		for (String dep: runtimeDependencies) {
			build.append("	implementation '"+dep+"'\n");
		}
		for (String dep: testCompileDependencies) {
			build.append("	testImplementation '"+dep+"'\n");
		}
		for (String dep: testRuntimeDependencies) {
			build.append("	testRuntimeOnly '"+dep+"'\n");
		}
		for (String dep: aptDependencies) {
			build.append("	apt '"+dep+"'\n");
		}
	}

	private void generateApplicationPluginDependencies(StringBuilder build) {
		for (String dep: patchDependencies) {
			build.append("	patch '"+dep+"'\n");
		}
		for (String dep: compileDependencies) {
			build.append("	compile '"+dep+"'\n");
		}
		for (String dep: runtimeDependencies) {
			build.append("	runtime '"+dep+"'\n");
		}
		for (String dep: testCompileDependencies) {
			build.append("	testImplementation '"+dep+"'\n");
		}
		for (String dep: testRuntimeDependencies) {
			build.append("	testRuntimeOnly '"+dep+"'\n");
		}
		for (String dep: aptDependencies) {
			build.append("	apt '"+dep+"'\n");
		}
	}

	private void generateJavaPluginDependencies(StringBuilder build) {
		for (String dep: patchDependencies) {
			build.append("	patch '"+dep+"'\n");
		}
		for (String dep: compileDependencies) {
			build.append("	compile '"+dep+"'\n");
		}
		for (String dep: runtimeDependencies) {
			build.append("	runtime '"+dep+"'\n");
		}
		for (String dep: testCompileDependencies) {
			build.append("	testCompile '"+dep+"'\n");
		}
		for (String dep: testRuntimeDependencies) {
			build.append("	testRuntime '"+dep+"'\n");
		}
		for (String dep: aptDependencies) {
			build.append("	apt '"+dep+"'\n");
		}
	}

	public File getRoot() {
		return tmpDir.getRoot();
	}

	private void createDirectories(String path) throws IOException {
				String[] splitPath = path.split("/");
		String[] updatedPath = new String[splitPath.length - 1];
		System.arraycopy(splitPath, 0, updatedPath, 0, splitPath.length - 1);

		File check = new File(tmpDir.getRoot(), String.join("/", updatedPath));
		if (!check.exists()) {
			tmpDir.newFolder(updatedPath);
		}
	}
}
