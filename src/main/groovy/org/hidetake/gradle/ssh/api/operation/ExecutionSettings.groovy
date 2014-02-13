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

    static final DEFAULT = new ExecutionSettings([:])
}
