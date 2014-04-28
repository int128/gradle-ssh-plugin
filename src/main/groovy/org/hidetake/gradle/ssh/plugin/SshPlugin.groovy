package org.hidetake.gradle.ssh.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hidetake.gradle.ssh.api.Remote

/**
 * Gradle SSH plugin.
 * This class adds project extension and convention properties.
 *
 * @see SshPluginConvention
 * @author hidetake.org
 *
 */
class SshPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.remotes = createRemoteContainer(project)
        project.convention.plugins.put('ssh', new SshPluginConvention())
    }

    private static createRemoteContainer(Project project) {
        def remotes = project.container(Remote)
        remotes.metaClass.mixin(RemoteContainerExtension)
        if (project.parent) {
            def parentRemotes = project.parent.extensions.remotes as NamedDomainObjectContainer<Remote>
            if (parentRemotes) {
                remotes.addAll(parentRemotes)
            }
        }
        remotes
    }
}
