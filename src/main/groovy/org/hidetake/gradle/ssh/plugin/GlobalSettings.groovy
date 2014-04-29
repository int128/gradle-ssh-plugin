package org.hidetake.gradle.ssh.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents global settings or task specific settings.
 *
 * @author hidetake.org
 *
 */
@EqualsAndHashCode
@ToString
class GlobalSettings extends Settings<GlobalSettings> {
    static final allowAnyHosts = ConnectionSettings.allowAnyHosts

    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    OperationSettings operationSettings = new OperationSettings()

    static final DEFAULT = new GlobalSettings(
            connectionSettings: ConnectionSettings.DEFAULT,
            operationSettings: OperationSettings.DEFAULT
    )

    @Override
    GlobalSettings plus(GlobalSettings right) {
        new GlobalSettings(
                connectionSettings: connectionSettings + right.connectionSettings,
                operationSettings: operationSettings + right.operationSettings
        )
    }
}
