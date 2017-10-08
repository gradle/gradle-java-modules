package com.zyxist.chainsaw.builder.factory

import com.zyxist.chainsaw.builder.JavaCodeFactory
import com.zyxist.chainsaw.builder.JigsawProjectBuilder

class DaggerModuleFactory implements JavaCodeFactory {
	static DaggerModuleFactory daggerModule() {
		return new DaggerModuleFactory()
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/ApplicationModule.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		return """
package ${builder.getPackageName()};

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
	@Provides
	public SomeClass providesSomeClass() {
		return new SomeClass();
	}
}
"""
	}
}
