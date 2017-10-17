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
package com.zyxist.chainsaw.jigsaw.cli;

import com.zyxist.chainsaw.jigsaw.JigsawFlags;

import java.util.List;

public class ModuleFlag implements ArgProducer {
	private final String moduleName;
	private final String mainClassName;

	public ModuleFlag(String moduleName, String mainClassName) {
		this.moduleName = moduleName;
		this.mainClassName = mainClassName;
	}

	public ModuleFlag() {
		this(null, null);
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getMainClassName() {
		return mainClassName;
	}

	@Override
	public void toArgs(List<String> args) {
		if (null != moduleName && null != mainClassName) {
			args.add(JigsawFlags.MODULE);
			args.add(moduleName + "/" + mainClassName);
		}
	}
}
