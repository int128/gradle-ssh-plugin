package org.hidetake.groovy.ssh.session.execution

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.SettingsHelper
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

trait SudoSettings {
    /**
     * Sudo password.
     */
    String sudoPassword

    /**
     * Sudo executable path.
     */
    String sudoPath

    /**
     * An input stream to send to the standard input.
     * @see org.hidetake.groovy.ssh.operation.CommandSettings#inputStream
     */
    def inputStream


    @EqualsAndHashCode
    static class With implements SudoSettings, ToStringProperties {
        def With() {}
        def With(SudoSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final SudoSettings DEFAULT = new SudoSettings.With(
                sudoPassword: null,
                sudoPath: 'sudo',
        )
    }
}
