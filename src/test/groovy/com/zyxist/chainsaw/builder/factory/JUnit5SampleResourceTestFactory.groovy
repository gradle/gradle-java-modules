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
package com.zyxist.chainsaw.builder.factory

import com.zyxist.chainsaw.builder.JavaCodeFactory
import com.zyxist.chainsaw.builder.JigsawProjectBuilder

class JUnit5SampleResourceTestFactory implements JavaCodeFactory {
	private boolean useModuleLoader

	static def junit5TestAccessingResources() {
		def factory = new JUnit5SampleResourceTestFactory()
		factory.useModuleLoader = false
		return factory
	}

	static def junit5TestAccessingModularResources() {
		def factory = new JUnit5SampleResourceTestFactory()
		factory.useModuleLoader = true
		return factory
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/SampleResourceTest.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		def loadingCode = useModuleLoader ? getModularLoadingCode() : getClasspathLoadingCode()
		return """
package ${builder.getPackageName()};

import java.util.Properties;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleResourceTest {
  @Test
  public void readsTestResources() throws IOException {
      Properties props = new Properties();
      props.load($loadingCode);

      assertEquals("Hello world", props.get("property"));
  }
}
"""
	}

	private String getClasspathLoadingCode() {
		return "getClass().getClassLoader().getResourceAsStream(\"text-resource.properties\")"
	}

	private String getModularLoadingCode() {
		return "getClass().getModule().getResourceAsStream(\"text-resource.properties\")"
	}
}
