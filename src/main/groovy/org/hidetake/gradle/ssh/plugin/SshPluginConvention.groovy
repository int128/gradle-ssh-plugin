package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

import static org.gradle.util.ConfigureUtil.configure

/**
 * Convention properties and methods.
 *
 * @author hidetake.org
 */
class SshPluginConvention {
    /**
     * Alias to omit import in build script.
     */
    final Class SshTask = org.hidetake.gradle.ssh.plugin.SshTask

    /**
     * Global settings.
     */
    protected final globalSettings = new GlobalSettings()

    /**
     * Configure global settings.
     *
     * @param closure closure for {@link GlobalSettings}
     */
    void ssh(Closure closure) {
        assert closure, 'closure should be set'
        configure(closure, globalSettings)
    }

    /**
     * Execute a task.
     *
     * @param closure closure for {@link SshTaskDelegate}
     */
    void sshexec(Closure closure) {
        assert closure, 'closure should be set'
        def delegate = new SshTaskDelegate()
        configure(closure, delegate)
        delegate.sessions.execute(
                ConnectionSettings.DEFAULT
                        + globalSettings.connectionSettings
                        + delegate.globalSettings.connectionSettings,
                OperationSettings.DEFAULT
                        + globalSettings.operationSettings
                        + delegate.globalSettings.operationSettings
        )
    }
}
