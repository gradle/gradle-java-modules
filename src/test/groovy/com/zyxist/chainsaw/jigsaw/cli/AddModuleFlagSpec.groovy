package com.zyxist.chainsaw.jigsaw.cli

import spock.lang.Specification

class AddModuleFlagSpec extends Specification {

	def "should not generate any CLI flag, if no extra modules are specified"() {
		given:
		def it = new AddModuleFlag()
		def args = []

		when:
		it.toArgs(args)

		then:
		args.isEmpty()
	}

	def "should handle a case, when one extra module is provided"() {
		given:
		def it = new AddModuleFlag()
			.add('com.example.foo')
		def args = []

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-modules'
		args.get(1) == 'com.example.foo'
	}

	def "should handle a case, when two or more extra modules are provided"() {
		given:
		def it = new AddModuleFlag()
			.add('com.example.foo')
			.add('com.example.bar')
		def args = []

		when:
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-modules'
		args.get(1) == 'com.example.foo,com.example.bar'
	}

	def "should add multiple module names from a collection, using addAll()"() {
		given:
		def it = new AddModuleFlag()
		def args = []

		when:
		it.addAll(['com.example.foo', 'com.example.bar'])
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-modules'
		args.get(1) == 'com.example.foo,com.example.bar'
	}

	def "should merge two AddModuleFlag instances, using merge()"() {
		given:
		def it = new AddModuleFlag().add('com.example.foo')
		def other = new AddModuleFlag().add('com.example.bar')
		def args = []

		when:
		it.merge(other)
		it.toArgs(args)

		then:
		args.size() == 2
		args.get(0) == '--add-modules'
		args.get(1) == 'com.example.foo,com.example.bar'
	}
}
