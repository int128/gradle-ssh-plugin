package org.hidetake.gradle.ssh

import org.gradle.util.ConfigureUtil
import org.hidetake.gradle.ssh.api.SshSpec
import org.hidetake.gradle.ssh.internal.DefaultSshService
import org.hidetake.gradle.ssh.internal.DryRunSshService

/**
 * Convention properties and methods.
 *
 * @author hidetake.org
 *
 */
class SshPluginConvention {
    protected final sshSpec = new SshSpec()
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
     * @param configureClosure closure for {@link SshSpec}
     */
    void ssh(Closure configureClosure) {
        assert configureClosure != null, 'configureClosure should be set'

        ConfigureUtil.configure(configureClosure, sshSpec)
        if (sshSpec.sessionSpecs.size() > 0) {
            throw new IllegalStateException('Do not declare any session in convention')
        }
    }

    /**
     * Executes SSH operations instead of a project task.
     *
     * @param configureClosure configuration closure for {@link SshSpec}
     */
    void sshexec(Closure configureClosure) {
        assert configureClosure != null, 'configureClosure should be set'

        def localSpec = new SshSpec()
        ConfigureUtil.configure(configureClosure, localSpec)
        def merged = SshSpec.computeMerged(localSpec, sshSpec)
        if (merged.dryRun) {
            dryRunService.execute(merged)
        } else {
            service.execute(merged)
        }
    }
}
