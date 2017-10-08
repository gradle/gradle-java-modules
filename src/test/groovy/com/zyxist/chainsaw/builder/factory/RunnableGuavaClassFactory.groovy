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
