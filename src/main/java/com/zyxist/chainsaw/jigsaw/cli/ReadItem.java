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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadItem implements ArgProducer {
	private final String srcModule;

	private final List<String> dstModules = new ArrayList<>();

	public ReadItem(String srcModule) {
		this.srcModule = srcModule;
	}

	public ReadItem to(String module) {
		this.dstModules.add(module);
		return this;
	}

	@Override
	public void toArgs(List<String> args) {
		if (!dstModules.isEmpty()) {
			args.add(JigsawFlags.ADD_READS);
			args.add(srcModule+"="+dstModules.stream().collect(Collectors.joining(",")));
		}
	}
}
