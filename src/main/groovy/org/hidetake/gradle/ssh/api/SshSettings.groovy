package org.hidetake.gradle.ssh.api

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

/**
 * Global SSH settings.
 *
 * @author hidetake.org
 *
 */
@EqualsAndHashCode
@ToString
class SshSettings {
    static final allowAnyHosts = ConnectionSettings.allowAnyHosts

    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    OperationSettings operationSettings = new OperationSettings()

    static final DEFAULT = new SshSettings(
            connectionSettings: ConnectionSettings.DEFAULT,
            operationSettings: OperationSettings.DEFAULT
    )

    SshSettings plus(SshSettings right) {
        new SshSettings(
                connectionSettings: connectionSettings + right.connectionSettings,
                operationSettings: operationSettings + right.operationSettings
        )
    }
}
