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

class TestNGSampleTestFactory implements JavaCodeFactory {
	boolean mocks;

	static def testngTest() {
		def factory = new TestNGSampleTestFactory()
		factory.mocks = false
		return factory
	}

	static def testngTestWithMocks() {
		def factory = new TestNGSampleTestFactory()
		factory.mocks = true
		return factory
	}

	@Override
	String getFilename(JigsawProjectBuilder builder) {
		return builder.getPackageName().replace(".", "/") + "/AClassTest.java"
	}

	@Override
	String generateCode(JigsawProjectBuilder builder) {
		def mockitoImport = ""
		def mockitoUsage = ""
		if (mocks) {
			mockitoImport = "import static org.mockito.Mockito.mock;"
			mockitoUsage = "Object obj = mock(Object.class);"
		}
		return """
package ${builder.getPackageName()};

${mockitoImport}
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

public class AClassTest {
  @Test
  public void isAnInstanceOfAClass() {
      ${mockitoUsage}
      assertTrue(new AImplementation() instanceof AClass);
  }
  
  static class AImplementation extends AClass {
    @Override
    public void aMethod(String aString) {
        // Do nothing
    }
  }
}
"""
	}
}
