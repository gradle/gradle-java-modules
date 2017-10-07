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
package com.zyxist.chainsaw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents information about a Java 9 module.
 */
public class JavaModule {
	private String moduleName;
	private boolean allowModuleNamingViolations = false;
	private List<String> addTestModules = new ArrayList<>();
	private Map<String, String> patchModules = new LinkedHashMap<>();

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

	public Map<String, String> getPatchModules() {
		return patchModules;
	}

	public void setPatchModules(Map<String, String> patchModules) {
		this.patchModules = patchModules;
	}
}
