/*
 * Copyright 2018 the original author or authors.
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

public class OpenItem implements ArgProducer {
	private final String openedModule;
	private final String openedPackage;
	private final String destinationModule;

	public OpenItem(String openedModule, String openedPackage, String destinationModule) {
		this.openedModule = openedModule;
		this.openedPackage = openedPackage;
		this.destinationModule = destinationModule;
	}

	public String getOpenedPackage() {
		return openedPackage;
	}

	public String getOpenedModule() {
		return openedModule;
	}

	public String getDestinationModule() {
		return destinationModule;
	}

	@Override
	public void toArgs(List<String> args) {
		args.add(JigsawFlags.ADD_OPENS);
		args.add(openedModule+"/"+openedPackage+"="+destinationModule);
	}
}
