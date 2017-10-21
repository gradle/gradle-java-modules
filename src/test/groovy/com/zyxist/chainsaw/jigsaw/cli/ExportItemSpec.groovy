package com.zyxist.chainsaw.jigsaw.cli

import spock.lang.Specification

class ExportItemSpec extends Specification {
	def "should not generate any CLI flag, if no exported modules are specified"() {
		given:
		def it = new ExportItem('com.example.foo', 'com.example.foo.pkg')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.isEmpty()
	}

	def "should correctly handle a case, when one module is exported"() {
		given:
		def it = new ExportItem('com.example.foo', 'com.example.foo.pkg')
			.to('com.example.bar')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-exports'
		args.get(1) == 'com.example.foo/com.example.foo.pkg=com.example.bar'
	}

	def "should correctly handle a case, when two or more modules are exported"() {
		given:
		def it = new ExportItem('com.example.foo', 'com.example.foo.pkg')
			.to('com.example.bar')
			.to('com.example.joe')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-exports'
		args.get(1) == 'com.example.foo/com.example.foo.pkg=com.example.bar,com.example.joe'
	}
}
