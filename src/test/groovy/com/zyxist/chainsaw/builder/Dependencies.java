/*
 * Copyright 2017-2018 the original author or authors.
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
package com.zyxist.chainsaw.builder;

public class Dependencies {
	public static final String JUNIT4_DEPENDENCY = "junit:junit:4.12";
	public static final String JUNIT5_PLUGIN_DEPENDENCY = "org.junit.platform:junit-platform-gradle-plugin:1.0.0";
	public static final String JUNIT5_API_DEPENDENCY = "org.junit.jupiter:junit-jupiter-api:5.0.0";
	public static final String JUNIT5_ENGINE_DEPENDENCY = "org.junit.jupiter:junit-jupiter-engine:5.0.0";
	public static final String MOCKITO_DEPENDENCY = "org.mockito:mockito-core:2.11.0";
	public static final String GUAVA_DEPENDENCY = "com.google.guava:guava:23.2-jre";
	public static final String DAGGER_DEPENDENCY = "com.google.dagger:dagger:2.11";
	public static final String DAGGER_COMPILER_DEPENDENCY = "com.google.dagger:dagger-compiler:2.11";

	public static final String JSR250_DEPENDENCY = "javax.annotation:jsr250-api:1.0";
	public static final String JSR305_DEPENDENCY = "com.google.code.findbugs:jsr305:1.3.9";

	public static final String JSR250_PATCH = "javax.annotation:jsr250-api";
	public static final String JSR305_PATCH = "com.google.code.findbugs:jsr305";

	public static final String DAGGER_MODULE = "dagger";
	public static final String GUAVA_MODULE = "com.google.common";
	public static final String JSR250_MODULE = "jsr250.api";
	public static final String MOCKITO_MODULE = "org.mockito";

	public static final String JUNIT5_PLUGIN_NAME = "org.junit.platform.gradle.plugin";

	public static final String APT_PLUGIN_VERSION = "0.15";
}
