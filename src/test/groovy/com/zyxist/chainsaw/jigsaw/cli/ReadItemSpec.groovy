package com.zyxist.chainsaw.jigsaw.cli

import spock.lang.Specification

class ReadItemSpec extends Specification {
	def "should not generate any CLI flag, if no modules opened for reading"() {
		given:
		def it = new ReadItem('com.example.foo')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.isEmpty()
	}

	def "should correctly handle a case, when one module is opened for reading"() {
		given:
		def it = new ReadItem('com.example.foo')
			.to('com.example.bar')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-reads'
		args.get(1) == 'com.example.foo=com.example.bar'
	}

	def "should correctly handle a case, when two or more modules are opened for reading"() {
		given:
		def it = new ReadItem('com.example.foo')
			.to('com.example.bar')
			.to('com.example.joe')
		def args = new LinkedList<String>()

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-reads'
		args.get(1) == 'com.example.foo=com.example.bar,com.example.joe'
	}
}
