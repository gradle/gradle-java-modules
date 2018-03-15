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
package com.zyxist.chainsaw.tests;

import com.zyxist.chainsaw.ChainsawPlugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Arrays;
import java.util.List;

public class TestNG implements TestEngine {
    private static final Logger LOGGER = Logging.getLogger(ChainsawPlugin.class);

    private static final String TESTNG_GROUP = "org.testng";
    private static final String TESTNG_ARTIFACT = "testng";

    private static final String TESTNG_MODULE_NAME = "testng";

    @Override
    public boolean accepts(Project project) {
        return TestEngine.checkTestDependencies(project, dep -> dep.getGroup().equals(TESTNG_GROUP) && dep.getName().equals(TESTNG_ARTIFACT));
    }

    @Override
    public List<String> getTestEngineModules() {
        return Arrays.asList(TESTNG_MODULE_NAME);
    }
}
