[![Build Status](https://travis-ci.org/zyxist/chainsaw.svg?branch=master)](https://travis-ci.org/zyxist/chainsaw)
[![Codecov](https://img.shields.io/codecov/c/github/zyxist/chainsaw.svg)]()
[![GitHub release](https://img.shields.io/github/release/zyxist/chainsaw.svg)]()

# Gradle Chainsaw Plugin

A plugin for [Gradle](http://gradle.org) that adds a support for Java Platform Module System, also known as Jigsaw,
introduced by Java 9.

Gradle doesn't currently support building Java 9 modules in a first-class way, except for a extremely limited
`experimental-jigsaw` plugin. This plugin aims to provide the necessary support by reconfiguring common Gradle
tasks to use modules, so that you could use modules in your project right now.

The full documentation and some tips, how to solve common problems with Jigsaw, can be found on wiki.

## Basic usage

If you already have a working Java project, applying the plugin is just one step:

```groovy
plugins {
  id 'com.zyxist.chainsaw' version 'x.y.z'
}
```


Chainsaw will automatically detect your Jigsaw module descriptor and reconfigure Gradle tasks
to use modules instead of classpath.

## Features

Every release brings improvements and support for more and more corner cases and use cases, based on
real-world projects. The plugin is already quite usable. All of the features:

 * support for modular compilation of main sources and unit tests,
 * support for running unit tests (JUnit 4/JUnit 5 with possibility to add additional test engines and test libraries),
 * support for javadocs,
 * support for `run` task,
 * support for custom Jigsaw flags and module patching,
 * partial support for `installDist` task.

The long-term vision includes providing support for building modular runtime images, and multirelease JAR
archives. To learn more about using Chainsaw, visit the project wiki.

## How to help?

Chainsaw is a community project. You can help in various ways:

 * give the plugin a star on Github or make a fork,
 * write about Chainsaw on your blog,
 * create a Pull Request.

When creating a Pull Request, remember to write integration tests for the new functionality. Jigsaw tends to be
very tricky and these tests are important to ensure that new features don't break anything that has previously worked.

## License

Apache License 2.0