package org.hidetake.gradle.ssh.plugin

import groovy.util.logging.Slf4j
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings

/**
 * Main class of Gradle SSH plugin.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SshPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.ssh = Ssh.newService()
        project.extensions.remotes = createRemoteContainer(project)
        project.extensions.proxies = createProxyContainer(project)

        project.ssh.settings.logging = OperationSettings.Logging.stdout

        // TODO: remove in future release
        project.ext.SshTask = SshTask
    }

    private static createRemoteContainer(Project project) {
        def remotes = project.container(Remote)
        remotes.metaClass.mixin(RemoteContainerExtension)
        def parentRemotes = project.parent?.extensions?.findByName('remotes')
        if (parentRemotes instanceof NamedDomainObjectContainer<Remote>) {
            remotes.addAll(parentRemotes)
        }
        remotes
    }

    private static createProxyContainer(Project project) {
		def proxies = project.container(Proxy)
		def parentProxies = project.parent?.extensions?.findByName('proxies')
		if (parentProxies instanceof NamedDomainObjectContainer<Proxy>) {
			proxies.addAll(parentProxies)
		}
		proxies
    }

    /**
     * Alternative entry point for old plugin ID, i.e. 'ssh'.
     *
     * @deprecated TODO: remove in future release
     */
    @Slf4j
    @Deprecated
    static class DeprecatedEntryPoint extends SshPlugin {
        @Override
        void apply(Project project) {
            log.warn "Deprecated: use apply plugin: 'org.hidetake.ssh', instead of 'ssh'"
            log.warn 'Deprecated: old plugin ID will be removed in future release'
            super.apply(project)
        }
    }
}
