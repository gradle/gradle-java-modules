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
package com.zyxist.chainsaw.jigsaw

import com.zyxist.chainsaw.jigsaw.cli.ExportItem
import com.zyxist.chainsaw.jigsaw.cli.OpenItem
import com.zyxist.chainsaw.jigsaw.cli.PatchItem
import com.zyxist.chainsaw.jigsaw.cli.ReadItem
import spock.lang.Specification

class JigsawCLISpec extends Specification {

	def "should generate --module-path"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')

		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe'
	}

	def "should generate --module with module name and main class"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')
			.module('com.example.foo', 'com.example.foo.Main')

		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --module com.example.foo/com.example.foo.Main'
	}

	def "should generate --module-version"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')
			.version("1.2.3")

		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --module-version 1.2.3'
	}

	def "should generate --add-modules"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe');
		cli.addModules()
			.add('com.example.foo')
			.add('com.example.bar')

		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --add-modules com.example.foo,com.example.bar'
	}

	def "should generate --add-exports"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')
		cli.exportList()
			.export(new ExportItem("some.module", "some.pkg").to("other.module"))


		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --add-exports some.module/some.pkg=other.module'
	}

	def "should generate --add-reads"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')
		cli.readList()
			.read(new ReadItem("some.module").to("other.module"))


		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --add-reads some.module=other.module'
	}

	def "should generate --add-opens"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')
		cli.openList()
			.open(new OpenItem("some.module", "opened.module", "dst.module"))


		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --add-opens some.module/opened.module=dst.module'
	}

	def "should generate --patch-module"() {
		given:
		def cli = new JigsawCLI('/foo/bar/joe')
		cli.patchList()
			.patch(new PatchItem("some.module").with("/path/to/some.jar"))


		when:
		def result = cli.toString()

		then:
		result == '--module-path /foo/bar/joe --patch-module some.module=/path/to/some.jar'
	}

	def "should return the provided module path"() {
		given:
		def cli = new JigsawCLI("/foo/bar/joe")

		when:
		def result = cli.getModulePath()

		then:
		result == '/foo/bar/joe'
	}
}
