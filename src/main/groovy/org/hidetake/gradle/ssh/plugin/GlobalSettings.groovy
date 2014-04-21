package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

/**
 * A delegate class for global settings.
 *
 * @author hidetake.org
 *
 */
class GlobalSettings {
    static final allowAnyHosts = ConnectionSettings.allowAnyHosts

    @Delegate
    protected final ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    protected final OperationSettings operationSettings = new OperationSettings()
}
