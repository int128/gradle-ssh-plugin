package org.hidetake.gradle.ssh.plugin

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote

/**
 * Main class of Gradle SSH plugin.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SshPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def registry = project.gradle.sharedServices
            .registerIfAbsent('sshRegistry', SshRegistryService) {}

        project.extensions.ssh = Ssh.newService()
        project.extensions.remotes = createRemoteContainer(project, registry)
        project.extensions.proxies = createProxyContainer(project, registry)

        project.ssh.settings.logging = 'stdout'

        project.ssh.metaClass.mixin(VersionExtension)

        // TODO
        if (project.gradle.gradleVersion =~ /^1\./) {
            log.warn('Gradle 1.x support will be removed in the future release. ' +
                'Please see https://github.com/int128/gradle-ssh-plugin/issues/230')
        }

        // TODO
        if (System.getProperty('java.version') =~ /^1\.6/) {
            log.warn('Java 6 support will be removed in the future release. ' +
                'Please see https://github.com/int128/gradle-ssh-plugin/issues/266')
        }
    }

    private static createRemoteContainer(Project project, def registry) {
        def remotes = project.container(Remote)
        remotes.metaClass.mixin(RemoteContainerExtension)
        remotes.addAll(registry.get().allRemotes())
        remotes.whenObjectAdded { Remote remote ->
            registry.get().registerRemote(remote)
        }
        remotes
    }

    private static createProxyContainer(Project project, def registry) {
        def proxies = project.container(Proxy)
        proxies.addAll(registry.get().allProxies())
        proxies.whenObjectAdded { Proxy proxy ->
            registry.get().registerProxy(proxy)
        }
        proxies
    }
}