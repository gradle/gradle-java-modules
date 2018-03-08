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
package com.zyxist.chainsaw.jigsaw.cli

import spock.lang.Specification

class OpenItemSpec extends Specification {
	def "should correctly handle a case, when one module is opened"() {
		given:
		def it = new OpenItem('java.base', 'java.lang', 'my.module')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-opens'
		args.get(1) == 'java.base/java.lang=my.module'
	}
}
