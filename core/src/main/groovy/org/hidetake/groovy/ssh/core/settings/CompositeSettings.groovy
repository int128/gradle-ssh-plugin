package org.hidetake.groovy.ssh.core.settings

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.connection.ConnectionSettings
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.session.SessionSettings

/**
 * Represents overall settings configurable in
 * {@link org.hidetake.groovy.ssh.core.Service#settings} and
 * {@link org.hidetake.groovy.ssh.core.RunHandler#settings}.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
class CompositeSettings implements PlusProperties<CompositeSettings>, ToStringProperties {
    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    SessionSettings sessionSettings = new SessionSettings()

    @Delegate
    CommandSettings commandSettings = new CommandSettings()

    static final DEFAULT = new CompositeSettings(
            connectionSettings: ConnectionSettings.DEFAULT,
            sessionSettings: SessionSettings.DEFAULT,
            commandSettings: CommandSettings.DEFAULT,
    )
}
