package org.hidetake.gradle.ssh.plugin

import groovy.util.logging.Slf4j
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.api.RunHandler
import org.hidetake.groovy.ssh.internal.DefaultRunHandler

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

/**
 * Gradle SSH plugin.
 * This class adds project extension and convention properties.
 *
 * @author hidetake.org
 */
@Slf4j
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
		def proxies = project.container(org.hidetake.groovy.ssh.api.Proxy)
		def parentProxies = project.parent?.extensions?.findByName('proxies')
		if (parentProxies instanceof NamedDomainObjectContainer<org.hidetake.groovy.ssh.api.Proxy>) {
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
         * @param closure closure for {@link org.hidetake.groovy.ssh.api.RunHandler}
         * @return returned value of the last session
         */
        Object sshexec(@DelegatesTo(RunHandler) Closure closure) {
            assert closure, 'closure must be given'
            def handler = new DefaultRunHandler()
            handler.metaClass.ssh = { Closure c ->
                log.info 'Deprecated: use settings {...} instead of ssh {...} in the sshexec method'
                handler.settings(c)
            }
            callWithDelegate(closure, handler)
            def settings = project.extensions.ssh as CompositeSettings
            handler.run(settings)
        }
    }
}
