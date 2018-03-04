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

class RunnableResourceClassFactory implements JavaCodeFactory {
	private boolean useModuleLoader

	static def runnableResourceClasspathClass() {
		def factory = new RunnableResourceClassFactory()
		factory.useModuleLoader = false
		return factory
	}

	static def runnableResourceModularClass() {
		def factory = new RunnableResourceClassFactory()
		factory.useModuleLoader = true
		return factory
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/ResourceClass.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		def loadingCode = useModuleLoader ? getModularLoadingCode() : getClasspathLoadingCode()
		return """
package ${builder.getPackageName()};
import java.util.Properties;
import java.io.IOException;

public class ResourceClass {
  public static void main(String... args) throws IOException {
    Properties props = new Properties();
    props.load($loadingCode);
    System.out.println(props.get("property"));
  }
}
"""
	}

	private String getClasspathLoadingCode() {
		return "ResourceClass.class.getClassLoader().getResourceAsStream(\"text-resource.properties\")"
	}

	private String getModularLoadingCode() {
		return "ResourceClass.class.getModule().getResourceAsStream(\"text-resource.properties\")"
	}
}
