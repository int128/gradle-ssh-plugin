package org.hidetake.gradle.ssh.api.operation

import groovy.transform.Immutable

/**
 * Settings for shell operation.
 *
 * @author hidetake.org
 */
@Immutable
class ShellSettings {
    boolean logging = true

    Closure interaction = null

    static final DEFAULT = new ShellSettings([:])
}
