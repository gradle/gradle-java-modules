package org.gradle.java.tests;

import org.gradle.java.JavaModule;

import java.util.List;

public class TestEngineUtils {
    public static void createModuleConfiguration(List<String> args, final JavaModule module, String testEngineModule) {
        args.add("--add-modules");
        args.add(testEngineModule + "," + joinStrings(module.getTestModules()));
        createReadModuleConfiguration(args, module, testEngineModule);
    }

    public static void createReadModuleConfiguration(List<String> args, final JavaModule module, String testEngineModule) {
        args.add("--add-reads");
        StringBuilder builder = new StringBuilder();
        builder.append(module.geName() + "=" + testEngineModule);
        for (String extraModule: module.getTestModules()) {
            builder.append(","+module.geName() + "=" + extraModule);
        }
        args.add(builder.toString());
    }

    private static String joinStrings(List<String> items) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String item: items) {
            if (!first) {
                builder.append(',');
            }
            builder.append(item);
            first = false;
        }
        return builder.toString();
    }
}
