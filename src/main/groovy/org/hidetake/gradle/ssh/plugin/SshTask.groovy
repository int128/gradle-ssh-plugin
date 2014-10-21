package org.hidetake.gradle.ssh.plugin

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.RunHandler
import org.hidetake.groovy.ssh.internal.DefaultRunHandler

/**
 * A SSH task for Gradle.
 *
 * @author hidetake.org
 */
@Slf4j
@Deprecated
class SshTask extends DefaultTask implements RunHandler {
    @Delegate
    private final RunHandler handler = new DefaultRunHandler()

    @Deprecated
    void ssh(@DelegatesTo(CompositeSettings) Closure closure) {
        log.warn 'Deprecated: use settings {...} instead of ssh {...} in the ssh task'
        handler.settings(closure)
    }

    @TaskAction
    void perform() {
        log.warn 'Deprecated: use ssh.run {...} instead of the ssh task'
        (handler as DefaultRunHandler).run(project.ssh.settings)
    }
}
