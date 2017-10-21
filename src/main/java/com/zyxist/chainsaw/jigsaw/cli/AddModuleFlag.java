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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zyxist.chainsaw.jigsaw.JigsawFlags.ADD_MODULES;

public class AddModuleFlag implements ArgProducer {
	private final List<String> extraModules = new LinkedList<>();

	public AddModuleFlag addAllModulePath() {
		this.add("ALL-MODULE-PATH");
		return this;
	}

	public AddModuleFlag add(String module) {
		this.extraModules.add(module);
		return this;
	}

	public AddModuleFlag addAll(Collection<String> modules) {
		this.extraModules.addAll(modules);
		return this;
	}

	public AddModuleFlag merge(AddModuleFlag otherFlag) {
		this.extraModules.addAll(otherFlag.extraModules);
		return this;
	}

	@Override
	public void toArgs(List<String> args) {
		if (!extraModules.isEmpty()) {
			args.add(ADD_MODULES);
			args.add(extraModules.stream().collect(Collectors.joining(",")));
		}
	}
}
