package org.hidetake.gradle.ssh.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Executor
import org.hidetake.gradle.ssh.registry.Registry
import org.hidetake.gradle.ssh.test.ConfineRegistryChanges
import spock.lang.Specification

@ConfineRegistryChanges
class SshTaskSpec extends Specification {

    static project() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'ssh'
            remotes {
                webServer {
                    host = 'web'
                    user = 'webuser'
                    identity = file('id_rsa')
                }
            }
            task(type: SshTask, 'testTask') {
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            it
        }
    }


    def "task action delegates to executor"() {
        given:
        def executor = Registry.instance[Executor] = Mock(Executor)

        def project = project()
        def task = project.tasks.testTask as SshTask

        def mergedMock = Mock(SshSettings)
        def globalSshSettings = project.convention.getPlugin(SshPluginConvention).ssh
        GroovySpy(SshSettings, global: true)
        1 * SshSettings.computeMerged(task.sshSettings, globalSshSettings) >> mergedMock

        when:
        task.perform()

        then:
        1 * executor.execute(mergedMock, _) >> { ignore, List<SessionSpec> sessions ->
            sessions.size() == 1
            sessions[0].remote == project.remotes.webServer
        }
    }

}
