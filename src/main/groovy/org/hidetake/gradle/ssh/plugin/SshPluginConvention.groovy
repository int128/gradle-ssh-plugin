package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Executor
import org.hidetake.gradle.ssh.registry.Registry

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
    final ssh = new SshSettings()

    private final executor = Registry.instance[Executor]

    /**
     * Configure global settings.
     *
     * @param closure closure for {@link SshSettings}
     */
    void ssh(Closure closure) {
        assert closure, 'closure should be set'
        configure(closure, ssh)
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
        def mergedSettings = SshSettings.computeMerged(delegate.sshSettings, ssh)
        executor.execute(mergedSettings, delegate.sessionSpecs)
    }
}
