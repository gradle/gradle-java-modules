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

import org.gradle.api.GradleException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility for safe rewriting text files, used for modifying startup scripts.
 * If preserves the platform line endings, allows replacing, removing and inserting
 * new lines.
 */
public class FileRewriter {
	private final File transformedFile;
	private final LineEndings eol;

	public FileRewriter(File transformedFile, LineEndings eol) {
		this.transformedFile = Objects.requireNonNull(transformedFile);
		this.eol = Objects.requireNonNull(eol);
	}

	public void rewrite(RewritingAlgorithm algorithm) {
		try {
			List<String> fileContent = new ArrayList<>(Files.readAllLines(transformedFile.toPath(), StandardCharsets.UTF_8));
			transformedFile.delete();
			try (BufferedWriter writer = Files.newBufferedWriter(transformedFile.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
				for (String srcLine : fileContent) {
					algorithm.rewrite(truncateEOL(srcLine), outLine -> {
						writer.write(outLine);
						writer.write(eol.getEnding());
					});
				}
			}
		} catch (IOException exception) {
			throw new GradleException("Cannot modify the file '" + transformedFile.getPath() + "'", exception);
		}
	}

	private String truncateEOL(String srcLine) {
		if (srcLine.endsWith("\r\n")) {
			return srcLine.substring(0, srcLine.length() - 2);
		} else if (srcLine.endsWith("\n")) {
			return srcLine.substring(0, srcLine.length() - 1);
		}
		return srcLine;
	}

	@FunctionalInterface
	public interface RewritingAlgorithm {
		void rewrite(String line, RewritingOutput output) throws IOException;
	}

	public enum LineEndings {
		WINDOWS("\r\n"),
		UNIX("\n");

		private final String ending;

		LineEndings(String ending) {
			this.ending = ending;
		}

		public String getEnding() {
			return ending;
		}
	}
}
