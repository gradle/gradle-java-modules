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
package com.zyxist.chainsaw.builder.factory

import com.zyxist.chainsaw.builder.JavaCodeFactory
import com.zyxist.chainsaw.builder.JigsawProjectBuilder

class RegularJavaClassFactory implements JavaCodeFactory {
	def name

	static RegularJavaClassFactory regularJavaClass(String name) {
		def factory = new RegularJavaClassFactory()
		factory.name = name
		return factory
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/" + name + ".java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		return """
package ${builder.getPackageName()};

public class ${name} {
  private String foo;

  public void setFoo(String foo) {
    this.foo = foo;
  }

  public void someMethod() {
    System.out.println(foo);
  }
  
  public void aMethod(String aString) {
    System.out.println(aString);
  }
}
"""
	}
}
