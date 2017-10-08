package com.zyxist.chainsaw.builder.factory

import com.zyxist.chainsaw.builder.JavaCodeFactory
import com.zyxist.chainsaw.builder.JigsawProjectBuilder

class RunnableDaggerClassFactory implements JavaCodeFactory {
	static RunnableDaggerClassFactory runnableDaggerClass() {
		return new RunnableDaggerClassFactory();
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/AClass.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		return """
package ${builder.getPackageName()};
public class AClass {
  public void aMethod(String aString) {
    SomeClass sc = DaggerApplicationComponent.create().someClass();
    sc.setFoo(aString);
    sc.someMethod();
      
  }
  
  public static void main(String... args) {
    new AClass().aMethod("Hello World!");
  }
}
"""
	}
}
