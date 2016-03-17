package org.hidetake.groovy.ssh.extension.settings

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.PlusProperties
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

@EqualsAndHashCode
class SudoSettings implements PlusProperties<SudoSettings>, ToStringProperties {
    /**
     * Sudo password.
     */
    String sudoPassword

    static final DEFAULT = new SudoSettings(
            sudoPassword: null,
    )
}
