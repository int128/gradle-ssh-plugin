package org.hidetake.gradle.ssh.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.api.session.SessionsFactory
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
        def sessions = Mock(Sessions)
        Registry.instance[SessionsFactory] = Mock(SessionsFactory) {
            create() >> sessions
        }
        def mergedMock = Mock(SshSettings)

        when:
        def project = project()
        def task = project.tasks.testTask as SshTask

        then:
        1 * sessions.add(_, _)

        when:
        def globalSshSettings = project.convention.getPlugin(SshPluginConvention).ssh
        GroovySpy(SshSettings, global: true)
        1 * SshSettings.computeMerged(task.sshSettings, globalSshSettings) >> mergedMock

        and:
        task.perform()

        then:
        1 * sessions.execute(mergedMock)
    }

}
