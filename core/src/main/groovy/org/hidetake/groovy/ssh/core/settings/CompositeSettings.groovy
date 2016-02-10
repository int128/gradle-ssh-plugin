package org.hidetake.groovy.ssh.core.settings

import org.hidetake.groovy.ssh.connection.ConnectionSettings
import org.hidetake.groovy.ssh.extension.settings.FileTransferSettings
import org.hidetake.groovy.ssh.extension.settings.SudoSettings
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.ShellSettings
import org.hidetake.groovy.ssh.session.SessionSettings

/**
 * Represents overall settings configurable in
 * {@link org.hidetake.groovy.ssh.core.Service#settings} and
 * {@link org.hidetake.groovy.ssh.core.RunHandler#settings}.
 *
 * @author Hidetake Iwata
 */
trait CompositeSettings implements
        ConnectionSettings,
        SessionSettings,
        CommandSettings,
        ShellSettings,
        SudoSettings,
        FileTransferSettings
{
    static class With implements CompositeSettings, ToStringProperties {
        def With() {}
        def With(CompositeSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final CompositeSettings DEFAULT = new CompositeSettings.With()
        static {
            SettingsHelper.mergeProperties(DEFAULT,
                    ConnectionSettings.With.DEFAULT,
                    SessionSettings.With.DEFAULT,
                    CommandSettings.With.DEFAULT,
                    ShellSettings.With.DEFAULT,
                    SudoSettings.With.DEFAULT,
                    FileTransferSettings.With.DEFAULT,
            )
        }
    }
}
