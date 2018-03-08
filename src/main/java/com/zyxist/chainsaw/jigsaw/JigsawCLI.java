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
package com.zyxist.chainsaw.jigsaw;

import com.zyxist.chainsaw.jigsaw.cli.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A model of Jigsaw command-line interface, used for generating the necessary
 * JVM and compiler CLI flags.
 */
public class JigsawCLI {
	private final String modulePath;
	private ModuleFlag moduleFlag = new ModuleFlag();
	private ModuleVersionFlag moduleVersion = new ModuleVersionFlag();
	private final AddModuleFlag addModuleFlag = new AddModuleFlag();
	private final PatchListFlag patchList = new PatchListFlag();
	private final ExportListFlag exportList = new ExportListFlag();
	private final ReadListFlag readList = new ReadListFlag();

	public JigsawCLI(String modulePath) {
		this.modulePath = modulePath;
	}

	public JigsawCLI module(String moduleName, String mainClassName) {
		this.moduleFlag = new ModuleFlag(moduleName, mainClassName);
		return this;
	}

	public JigsawCLI version(String version) {
		this.moduleVersion = new ModuleVersionFlag(version);
		return this;
	}

	public AddModuleFlag addModules() {
		return this.addModuleFlag;
	}

	public PatchListFlag patchList() {
		return this.patchList;
	}

	public ExportListFlag exportList() {
		 return this.exportList;
	}

	public ReadListFlag readList() {
		return this.readList;
	}

	public void toArgs(List<String> args) {
		generateModulePath(args);
		moduleVersion.toArgs(args);
		addModuleFlag.toArgs(args);
		exportList.toArgs(args);
		readList.toArgs(args);
		patchList.toArgs(args);
		moduleFlag.toArgs(args);
	}

	public List<String> generateArgs() {
		List<String> args = new ArrayList<>();
		toArgs(args);
		return args;
	}

	@Override
	public String toString() {
		return generateArgs().stream().collect(Collectors.joining(" "));
	}

	public String getModulePath() {
		return modulePath;
	}

	private void generateModulePath(List<String> args) {
		args.add(JigsawFlags.MODULE_PATH);
		args.add(modulePath);
	}
}
