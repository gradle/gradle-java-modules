import org.gradle.api.JavaVersion.VERSION_1_7

plugins {
    groovy
    idea
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.9.7"
}

group = "org.gradle.java"

version = "0.1.1"

java {
    targetCompatibility = VERSION_1_7
    sourceCompatibility = VERSION_1_7
}

// Fix a bad interaction with IntelliJ and Gradle > 4.0
idea.module.inheritOutputDirs = true

// make the publishing plugin skip checks that disallow publishing to com.gradle / org.gradle groups
System.setProperty("gradle.publish.skip.namespace.check", "true")

repositories {
    jcenter()
}

dependencies {
    testCompile ("org.spockframework:spock-core:1.1-groovy-2.4") {
        exclude(module = "groovy-all")
    }
}

gradlePlugin {
    (plugins) {
        "experimentalJigsawPlugin" {
            id = "org.gradle.java.experimental-jigsaw"
            implementationClass = "org.gradle.java.JigsawPlugin"
        }
    }
}

pluginBundle {
    website = "https://guides.gradle.org/building-java-9-modules"
    vcsUrl = "https://github.com/gradle/gradle-java-modules"
    (plugins) {
        "experimentalJigsawPlugin" {
            id = "org.gradle.java.experimental-jigsaw"
            displayName = "Experimental Jigsaw Plugin"
            description = "Experiment with Java 9 modules before they are officially supported."
            tags = listOf("jigsaw", "modules", "java9")
        }
    }
}
