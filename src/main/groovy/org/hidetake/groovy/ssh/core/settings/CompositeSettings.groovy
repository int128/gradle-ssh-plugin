package org.hidetake.groovy.ssh.core.settings

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents overall settings configurable in global or task.
 *
 * @author hidetake.org
 */
@EqualsAndHashCode
@ToString
class CompositeSettings extends Settings<CompositeSettings> {
    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    OperationSettings operationSettings = new OperationSettings()

    static final DEFAULT = new CompositeSettings(
            connectionSettings: ConnectionSettings.DEFAULT,
            operationSettings: OperationSettings.DEFAULT
    )

    @Override
    CompositeSettings plus(CompositeSettings right) {
        new CompositeSettings(
                connectionSettings: connectionSettings + right.connectionSettings,
                operationSettings: operationSettings + right.operationSettings
        )
    }
}
