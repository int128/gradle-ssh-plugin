package org.hidetake.gradle.ssh.plugin

import org.gradle.api.Project

/**
 * An extension to provide access to the {@link Project}.
 *
 * @author Hidetake Iwata
 */
trait ProjectInjection {

    static class Locator {
        protected static Project project
    }

    /**
     * Return the project.
     *
     * @return the project
     */
    Project getProject() {
        Locator.project
    }

}