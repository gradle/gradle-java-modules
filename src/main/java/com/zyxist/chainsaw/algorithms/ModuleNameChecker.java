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
package com.zyxist.chainsaw.algorithms;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;

/**
 * Tests that the module name is derived from the root package
 * (the official recommendation for module naming).
 */
public class ModuleNameChecker {
	private static final Logger LOGGER = Logging.getLogger(ModuleNameChecker.class);
	private final String mainDirectory;

	public ModuleNameChecker(String mainDirectory) {
		this.mainDirectory = mainDirectory;
	}

	public boolean verifyModuleName(String moduleName) {
		String pkgDirName = mainDirectory + "/" + asPackageDirectory(moduleName);
		File pkgDir = new File(pkgDirName);
		LOGGER.info("Testing module name '"+moduleName+"' against package '" + pkgDirName + "'");
		return pkgDir.exists() && pkgDir.isDirectory();
	}

	private String asPackageDirectory(String moduleName) {
		return moduleName.replace(".", "/");
	}
}
