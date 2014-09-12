package org.hidetake.gradle.ssh.plugin

import groovy.util.logging.Slf4j
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hidetake.gradle.ssh.internal.SshTaskService

/**
 * Gradle SSH plugin.
 * This class adds project extension and convention properties.
 *
 * @author hidetake.org
 */
class SshPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.ssh = new CompositeSettings()
        project.extensions.remotes = createRemoteContainer(project)
        project.extensions.proxies = createProxyContainer(project)

        project.convention.plugins.ssh = new Convention(project)
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

    static class Convention {
        private final Project project

        private Convention(Project project1) {
            project = project1
            assert project
        }

        /**
         * Alias to omit import in build script.
         */
        final Class SshTask = org.hidetake.gradle.ssh.plugin.SshTask

        /**
         * Execute a SSH closure.
         *
         * @param closure closure for {@link org.hidetake.gradle.ssh.internal.DefaultSshTaskHandler}
         * @return returned value of the last session
         */
        Object sshexec(Closure closure) {
            assert closure, 'closure must be given'
            SshTaskService.instance.execute(project.extensions.ssh as CompositeSettings, closure)
        }
    }

    /**
     * Alternative entry point for old plugin ID, i.e. 'ssh'.
     * @deprecated will be removed in future
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
