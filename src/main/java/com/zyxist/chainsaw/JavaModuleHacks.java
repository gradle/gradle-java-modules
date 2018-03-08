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
package com.zyxist.chainsaw;

import com.zyxist.chainsaw.jigsaw.JigsawCLI;
import com.zyxist.chainsaw.jigsaw.cli.ExportItem;
import com.zyxist.chainsaw.jigsaw.cli.OpenItem;
import com.zyxist.chainsaw.jigsaw.cli.ReadItem;

import java.util.*;

/**
 * This DSL element allows generating additional custom Jigsaw flags, such as --add-opens or
 * --patch-module, which are necessary to hack various older libraries which break Jigsaw
 * rules.
 */
public class JavaModuleHacks {
	private List<OpenItem> customOpenItems = new ArrayList<>();
	private List<ReadItem> customReadItems = new ArrayList<>();
	private List<ExportItem> customExportItems = new ArrayList<>();
	private Map<String, String> patchedDependencies = new LinkedHashMap<>();

	public void opens(String openingModule, String openedModule, String destinationModule) {
		this.customOpenItems.add(new OpenItem(openingModule, openedModule, destinationModule));
	}

	public void reads(String srcModule, String dstModule) {
		this.customReadItems.add(new ReadItem(srcModule).to(dstModule));
	}

	public void reads(String srcModule, Collection<String> modules) {
		this.customReadItems.add(new ReadItem(srcModule).toAll(modules));
	}

	public void exports(String module, String exportedPackage, String dstModule) {
		this.customExportItems.add(new ExportItem(module, exportedPackage).to(dstModule));
	}

	public void exports(String module, String exportedPackage, Collection<String> dstModules) {
		this.customExportItems.add(new ExportItem(module, exportedPackage).toAll(dstModules));
	}

	public void patches(String patchedDependency, String patchingDependency) {
		this.patchedDependencies.put(patchedDependency, patchingDependency);
	}

	public List<OpenItem> getCustomOpenItems() {
		return customOpenItems;
	}

	public List<ReadItem> getCustomReadItems() {
		return customReadItems;
	}

	public List<ExportItem> getCustomExportItems() {
		return customExportItems;
	}

	public Map<String, String> getPatchedDependencies() {
		return patchedDependencies;
	}

	public void applyHacks(JigsawCLI cli) {
		getCustomOpenItems().forEach(openDef -> cli.openList().open(openDef));
		getCustomReadItems().forEach(readDef -> cli.readList().read(readDef));
		getCustomExportItems().forEach(exportDef -> cli.exportList().export(exportDef));
	}
}
