[![Build Status](https://travis-ci.org/zyxist/chainsaw.svg?branch=master)](https://travis-ci.org/zyxist/chainsaw)
[![Codecov](https://img.shields.io/codecov/c/github/zyxist/chainsaw.svg)]()
[![GitHub release](https://img.shields.io/github/release/zyxist/chainsaw.svg)]()

# Chainsaw

A plugin for [Gradle](http://gradle.org) that adds a support for Java Platform Module System, also known as Jigsaw,
introduced by Java 9.

Gradle doesn't currently support building Java 9 modules in a first-class way, except for a extremely limited
`experimental-jigsaw` plugin. I decided to fork the plugin and extend it with a couple of necessary features.

## Basic usage

If you already have a working Java project, applying the plugin is just two steps:

 1. Apply the plugin:
    ```groovy
    plugins {
      id 'com.zyxist.chainsaw' version 'x.y.z'
    }
    ```
 2. Set the module name:
    ```groovy
    javaModule.name = '<your-module-name>'
    ```

The module name must be identical, as in `module-info.java` descriptor.

### Naming modules

Modules are tightly related to packages. The official recommendation is to use reverse-DNS style, and name your
module from the root package. By default, the plugin enforces this convention and checks whether there is a single,
root package in `/src/main/java` corresponding to the module name.

If you are migrating an existing code to modules, and the module name cannot match the recommendation, you can disable
the check:

```groovy
javaModule.allowModuleNamingViolations = true
```

Remember that Java 9 does not allow package splits (using identical packages in multiple modules).

## Unit tests

The plugin recognizes JUnit 4 and JUnit 5 dependencies and configures the test launcher accordingly. Unit tests are
attached to the main code through the `--patch-module` flag, so they are considered by the JVM as a part of your
module, and have the unrestricted access to the code being tested.

To add extra test modules, such as mocking library, or an unsupported test framework, use the following option:

```groovy
javaModule.extraTestModules = ['org.mockito']

dependencies {
   testCompile('org.mockito:mockito-core:x.y.z')
}
```

## Patching

Some commonly used dependencies violate the *no package split* rule. For example, `javax.validation` package can be
found in several JAR-s: `jsr305`, `jsr250`, and the deprecated JDK module `java.xml.ws.annotation`. By default, they
cannot be used together, unless we patch them. Module patching is a way to combine multiple JAR archives into a
single automatic module in runtime that helps dealing with the issue, until the correctly packaged alternatives
become available.

In the example below, our project is already using official JSR-250 as a dependency. We want to add a dependency to
Google Guava, which (as of version 23.1) is known to use rogue JAR `jsr305`. Both dependencies cannot co-exist together,
because of a package split. We can patch `jsr250.api` module to include all the annotations from `jsr305`:
 

```groovy
javaModule.patchModules 'com.google.code.findbugs:jsr305': 'javax.annotation:jsr250-api'

dependencies {
    patch 'com.google.code.findbugs:jsr305:1.3.9'
    
    compile 'javax.annotation:jsr250-api:1.0'
    compile 'com.google.guava:guava:23.2-jre'
}
```

The module descriptor would look like this:

```java
module com.example.application {
    requires jsr250.api; // warning: this module name is generated and it is not safe to rely on it!
    requires com.google.common;
}
```

How patching works:

 * the patched dependency must be added to `patch` configuration,
 * the plugin removes the patched dependencies from all other configurations,
 * the plugin generates the necessary `--patch-module` flags for the compiler and JVM.
 
Usage notes:
 * If you are using the annotation processing plugin `net.ltgt.apt`, the plugin must be applied AFTER Chainsaw!
 * Patching works for compiling, testing and running the application from Gradle. However, if you are going
to run the executable JAR manually, e.g. using a shell script, you have to add all the necessary `--patch-module` command
line switches to the JVM on your own.

## License

Apache License 2.0