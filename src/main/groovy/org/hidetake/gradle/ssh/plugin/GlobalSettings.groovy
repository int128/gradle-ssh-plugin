package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

/**
 * Represents global settings or task specific settings.
 *
 * @author hidetake.org
 *
 */
class GlobalSettings {
    static final allowAnyHosts = ConnectionSettings.allowAnyHosts

    @Delegate
    final ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    final OperationSettings operationSettings = new OperationSettings()
}
