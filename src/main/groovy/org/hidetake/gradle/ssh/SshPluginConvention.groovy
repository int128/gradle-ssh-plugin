package org.hidetake.gradle.ssh

import org.gradle.util.ConfigureUtil
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.internal.DefaultSshService
import org.hidetake.gradle.ssh.internal.DryRunSshService

/**
 * Convention properties and methods.
 *
 * @author hidetake.org
 *
 */
class SshPluginConvention {
    protected final sshSettings = new SshSettings()
    protected service = DefaultSshService.instance
    protected dryRunService = DryRunSshService.instance

    /**
     * Alias to omit import in the build script.
     */
    final SshTask = classOfSshTask

    // fixes inspection warning (Unnecessary fully qualified name)
    private static final classOfSshTask = SshTask

    /**
     * Configures global settings.
     *
     * @param configureClosure closure for {@link SshSettings}
     */
    void ssh(Closure configureClosure) {
        assert configureClosure != null, 'configureClosure should be set'

        ConfigureUtil.configure(configureClosure, sshSettings)
        if (sshSettings.sessionSpecs.size() > 0) {
            throw new IllegalStateException('Do not declare any session in convention')
        }
    }

    /**
     * Executes SSH operations instead of a project task.
     *
     * @param configureClosure configuration closure for {@link SshSettings}
     */
    void sshexec(Closure configureClosure) {
        assert configureClosure != null, 'configureClosure should be set'

        def localSpec = new SshSettings()
        ConfigureUtil.configure(configureClosure, localSpec)
        def merged = SshSettings.computeMerged(localSpec, sshSettings)
        if (merged.dryRun) {
            dryRunService.execute(merged)
        } else {
            service.execute(merged)
        }
    }
}
