package org.hidetake.gradle.ssh.plugin

import org.hidetake.gradle.ssh.api.SshSettings

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
        delegate.sessions.execute(SshSettings.DEFAULT + ssh + delegate.sshSettings)
    }
}
