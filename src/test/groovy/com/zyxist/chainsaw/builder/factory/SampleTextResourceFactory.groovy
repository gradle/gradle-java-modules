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

class SampleTextResourceFactory implements JavaCodeFactory {
	private boolean inPackage = false

	static SampleTextResourceFactory sampleTextResource() {
		return new SampleTextResourceFactory()
	}

	static SampleTextResourceFactory samplePackagedTextResource() {
		def factory = new SampleTextResourceFactory()
		factory.inPackage = true
		return factory
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return (inPackage ? builder.getPackageName().replace(".", "/") + "/" : "") + "text-resource.properties"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		return """
property=Hello world
"""
	}
}
