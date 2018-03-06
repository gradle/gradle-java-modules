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
package com.zyxist.chainsaw.algorithms;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.github.javaparser.Providers.provider;

public class ModuleNameDetector {
	private final JavaParser parser;
	private final ParserConfiguration.LanguageLevel languageLevel;

	public ModuleNameDetector(JavaVersion javaVersion) {
		this.languageLevel = translateJavaVersion(javaVersion);
		this.parser = new JavaParser(new ParserConfiguration()
			.setLanguageLevel(languageLevel));
	}

	public ParserConfiguration.LanguageLevel getLanguageLevel() {
		return languageLevel;
	}

	public String findModuleName(File sourceDir) {
		try {
			File moduleDescriptor = new File(sourceDir, "module-info.java");
			if (!moduleDescriptor.exists()) {
				throw new GradleException("The project is lacking a Java module descriptor in '" + sourceDir.getPath() + "' directory.");
			}
			ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(new FileInputStream(moduleDescriptor)));
			if (result.isSuccessful()) {
				return result.getResult().get().getModule()
					.map(module -> module.getName().asString())
					.orElseThrow(() -> new GradleException("Extracting Java module name failed."));
			}
			throw new GradleException("Unable to parse the Java module descriptor.");
		} catch (FileNotFoundException exception) {
			throw new GradleException("Cannot find the module descriptor.", exception);
		}
	}
	private ParserConfiguration.LanguageLevel translateJavaVersion(JavaVersion version) {
		switch (version) {
			case VERSION_1_9:
				return ParserConfiguration.LanguageLevel.JAVA_9;
			case VERSION_1_10:
				return ParserConfiguration.LanguageLevel.JAVA_10;
			default:
				return ParserConfiguration.LanguageLevel.JAVA_9;
		}
	}
}
