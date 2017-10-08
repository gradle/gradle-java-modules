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

class RunnableGuavaClassFactory implements JavaCodeFactory {
	static RunnableGuavaClassFactory runnableGuavaJsr250Class() {
		return new RunnableGuavaClassFactory();
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/AClass.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		return """
package ${builder.getPackageName()};

import javax.annotation.Resource;
import com.google.common.collect.ImmutableList;

@Resource
public class AClass {
  public void aMethod(String aString) {
    ImmutableList<String> list = ImmutableList.of(aString);
    System.out.println(list.get(0));
  }
  
  public static void main(String... args) {
    new AClass().aMethod("Hello World!");
  }
}
"""
	}
}
