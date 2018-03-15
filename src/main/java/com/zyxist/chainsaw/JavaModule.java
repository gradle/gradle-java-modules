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

import org.gradle.api.Action;

import java.util.*;

/**
 * Represents information about a Java 9 module.
 */
public class JavaModule {
	private String moduleName;
	private boolean allowModuleNamingViolations = false;
	private List<String> addTestModules = new ArrayList<>();
	private List<String> exportTestPackages = new ArrayList<>();
	private JavaModuleHacks hacks = new JavaModuleHacks();

	public String getName() {
		return moduleName;
	}

	public void setName(String moduleName) {
		this.moduleName = moduleName;
	}

	public boolean isAllowModuleNamingViolations() {
		return allowModuleNamingViolations;
	}

	public void setAllowModuleNamingViolations(boolean allowModuleNamingViolations) {
		this.allowModuleNamingViolations = allowModuleNamingViolations;
	}

	public List<String> getExtraTestModules() {
		return addTestModules;
	}

	public void setExtraTestModules(List<String> testModules) {
		this.addTestModules = testModules;
	}

	public List<String> getExportedTestPackages() {
		return exportTestPackages;
	}

	public void setExportedTestPackages(List<String> testPackages) {
		this.exportTestPackages = testPackages;
	}

	public void hacks(Action<? super JavaModuleHacks> action) {
		action.execute(hacks);
	}

	public JavaModuleHacks getHacks() {
		return hacks;
	}
}
