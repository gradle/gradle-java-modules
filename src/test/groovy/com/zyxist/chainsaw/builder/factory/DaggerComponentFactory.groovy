package com.zyxist.chainsaw.builder.factory

import com.zyxist.chainsaw.builder.JavaCodeFactory
import com.zyxist.chainsaw.builder.JigsawProjectBuilder

class DaggerComponentFactory implements JavaCodeFactory {
	static DaggerComponentFactory daggerComponent() {
		return new DaggerComponentFactory()
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/ApplicationComponent.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		return """
package ${builder.getPackageName()};

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = { ApplicationModule.class }
)
public interface ApplicationComponent {
	SomeClass someClass();
}
"""
	}
}
