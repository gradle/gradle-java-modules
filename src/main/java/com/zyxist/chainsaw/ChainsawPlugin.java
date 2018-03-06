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
package com.zyxist.chainsaw;

import com.zyxist.chainsaw.algorithms.ModuleNameDetector;
import com.zyxist.chainsaw.compilation.CompileJavaConfigurator;
import com.zyxist.chainsaw.compilation.GenerateJavadocConfigurator;
import com.zyxist.chainsaw.exec.CreateStartScriptsConfigurator;
import com.zyxist.chainsaw.exec.RunTaskConfigurator;
import com.zyxist.chainsaw.tasks.VerifyModuleNameTask;
import com.zyxist.chainsaw.tests.*;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.internal.impldep.org.bouncycastle.math.raw.Mod;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChainsawPlugin implements Plugin<Project> {
	private static final Logger LOGGER = Logging.getLogger(ChainsawPlugin.class);

	private static final String APPLICATION_PLUGIN = "application";

	public static final String PATCH_CONFIGURATION_NAME = "patch";

	private static final String EXTENSION_NAME = "javaModule";

	private static final String VERIFY_MODULE_NAME_TASK_NAME = "verifyModuleName";

	private static final TestEngine[] TEST_ENGINES = {
		new JUnit4(),
		new JUnit5(),
		new NoTestEngine()
	};

	@Override
	public void apply(Project project) {
		LOGGER.debug("Applying ChainsawPlugin to " + project.getName());
		JavaPlugin plugin = project.getPlugins().apply(JavaPlugin.class);
		Configuration cfg = project.getConfigurations().create(PATCH_CONFIGURATION_NAME);
		cfg.setDescription("Dependencies that break Java Module System rules and need to be added as patches to other modules.");

		project.getExtensions().create(EXTENSION_NAME, JavaModule.class);
		JavaPluginConvention javaConfig = project.getConvention().getPlugin(JavaPluginConvention.class);
		ModuleNameDetector detector = new ModuleNameDetector(javaConfig.getSourceCompatibility());

		VerifyModuleNameTask vmnTask = project.getTasks().create(VERIFY_MODULE_NAME_TASK_NAME, VerifyModuleNameTask.class);
		vmnTask.dependsOn("compileJava");

		removePatchedDependencies(project, cfg);
		configureJavaTasks(project, detector);
	}

	private void removePatchedDependencies(Project project, Configuration patchConfig) {
		project.getConfigurations().all(cfg -> {
			if (!cfg.getName().equals(PATCH_CONFIGURATION_NAME)) {
				patchConfig.getDependencies().all(dependency -> {
					Map<String, String> exclusion = new LinkedHashMap<>();
					exclusion.put("group", dependency.getGroup());
					exclusion.put("module", dependency.getName());
					cfg.exclude(exclusion);
				});
			}
		});
	}

	private TestEngine selectTestEngine(Project project) {
		for (TestEngine testEngine : TEST_ENGINES) {
			if (testEngine.accepts(project)) {
				return testEngine;
			}
		}
		throw new GradleException("Unknown test engine.");
	}

	private void inferModuleName(Project project, JavaModule moduleConfig, ModuleNameDetector detector) {
		if (moduleConfig.getName() == null) {
			final SourceSet mainSourceSet = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
			moduleConfig.setName(detector.findModuleName(mainSourceSet.getJava().getSrcDirs().iterator().next()));
			LOGGER.info("Inferred module name: " + moduleConfig.getName());
		}
	}

	private void configureJavaTasks(final Project project, final ModuleNameDetector detector) {
		project.afterEvaluate(evaluatedProject -> {
			final JavaModule moduleConfig = (JavaModule) evaluatedProject.getExtensions().getByName(EXTENSION_NAME);
			inferModuleName(evaluatedProject, moduleConfig, detector);
			TestEngine testEngine = selectTestEngine(evaluatedProject);
			final TaskConfigurationOrchestrator orchestrator = new TaskConfigurationOrchestrator();
			orchestrator
				.use(ConfigurableTask.JAVA_COMPILE, new CompileJavaConfigurator(moduleConfig))
				.use(ConfigurableTask.JAVA_TEST_COMPILE, new CompileTestJavaConfigurator(moduleConfig, testEngine))
				.use(ConfigurableTask.JAVADOC, new GenerateJavadocConfigurator(moduleConfig))
				.use(ConfigurableTask.TEST, new TestTaskConfigurator(moduleConfig, testEngine))
				.use(ConfigurableTask.RUN, new RunTaskConfigurator(moduleConfig))
				.use(ConfigurableTask.START_SCRIPTS, new CreateStartScriptsConfigurator(moduleConfig))
				.configureTasks(evaluatedProject, "java");

			evaluatedProject.getPluginManager()
				.withPlugin(APPLICATION_PLUGIN, appliedPlugin -> orchestrator.configureTasks(evaluatedProject, "application"));
		});
	}
}
