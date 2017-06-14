package org.gradle.java;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class JigsawPlugin implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(JigsawPlugin.class);

    @Override
    public void apply(Project project) {
        LOGGER.debug("Applying JigsawPlugin to " + project.getName());
    }
}
