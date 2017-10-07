[![Build Status](https://travis-ci.org/zyxist/chainsaw.svg?branch=master)](https://travis-ci.org/zyxist/chainsaw)

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
      id 'com.zyxist.chainsaw' version '0.1.2'
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
the check (to be implemented):

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

(to be implemented)

Some commonly used dependencies violate the *no package split* rule. For example, `javax.validation` package can be
found in several JAR-s: `jsr305`, `jsr250`, and the deprecated JDK module `java.xml.ws.annotation`. By default, they
cannot be used together, unless we patch them. Module patching is a way to combine multiple JAR archives into a
single automatic module in runtime that helps dealing with the issue, until the correctly packaged alternatives
become available.

```groovy
javaModule.patchModules = [
    'jsr305:jsr305' => 'jsr250'
    'group:dependency' => 'destinationModule'
]
```


Note that patching options are not preserved in the output artifact. If the archive is an executable JAR, or it is
going to be used as a dependency for an executable JAR, you must add the `--patch-module` arguments to the `java`
command manually.

## License

Apache License 2.0