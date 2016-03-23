package org.hidetake.groovy.ssh.extension.settings

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
