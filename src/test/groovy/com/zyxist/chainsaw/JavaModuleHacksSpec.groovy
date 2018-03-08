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
package com.zyxist.chainsaw

import com.zyxist.chainsaw.jigsaw.JigsawCLI
import spock.lang.Specification

class JavaModuleHacksSpec extends Specification {

	def "it should apply all customizations to the Jigsaw CLI"() {
		given:
		JigsawCLI cli = new JigsawCLI();
		JavaModuleHacks hacks = new JavaModuleHacks();
		hacks.opens("a", "b", "c")
		hacks.reads("a", "b")
		hacks.exports("mod", "pkg", "other.mod")

		when:
		hacks.applyHacks(cli)
		def args = cli.generateArgs()

		then:
		args.contains("--add-opens")
		args.contains("a/b=c")
		args.contains("--add-reads")
		args.contains("a=b")
		args.contains("--add-exports")
		args.contains("mod/pkg=other.mod")
	}

	def "it is possible to add multiple destinations to export"() {
		given:
		JigsawCLI cli = new JigsawCLI();
		JavaModuleHacks hacks = new JavaModuleHacks();
		hacks.exports("mod", "pkg", ["abc", "def"])

		when:
		hacks.applyHacks(cli)
		def args = cli.generateArgs()

		then:
		args.contains("--add-exports")
		args.contains("mod/pkg=abc,def")
	}

	def "it is possible to add multiple destinations to reads"() {
		given:
		JigsawCLI cli = new JigsawCLI();
		JavaModuleHacks hacks = new JavaModuleHacks()
		hacks.reads("mod", ["abc", "def"])

		when:
		hacks.applyHacks(cli)
		def args = cli.generateArgs()

		then:
		args.contains("--add-reads")
		args.contains("mod=abc,def")
	}

	def "it is possible to add patch dependencies"() {
		given:
		JavaModuleHacks hacks = new JavaModuleHacks()

		when:
		hacks.patches("com.example.group:artifactId", "com.example.else:something")

		then:
		hacks.patchedDependencies.size() == 1
		hacks.patchedDependencies['com.example.group:artifactId'] == 'com.example.else:something'
	}
}
