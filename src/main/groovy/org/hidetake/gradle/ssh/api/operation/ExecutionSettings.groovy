package org.hidetake.gradle.ssh.api.operation

import groovy.transform.Immutable

/**
 * Settings for command execution.
 *
 * @author hidetake.org
 */
@Immutable
class ExecutionSettings {
    boolean pty = false

    boolean logging = true

    Closure interaction = null

    /**
     * Compute a merged settings.
     *
     * @param map key(s) and value(s) to override
     * @return a merged settings
     */
    ExecutionSettings plus(Map map) {
        new ExecutionSettings((properties + map) as HashMap)
    }

    static final DEFAULT = new ExecutionSettings([:])
}
