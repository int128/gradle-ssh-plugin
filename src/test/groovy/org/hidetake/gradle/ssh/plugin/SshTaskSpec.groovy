package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.session.Sessions
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.test.RegistryHelper.factoryOf

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
            task(type: SshTask, 'testTask1') {
                ssh {
                    knownHosts = allowAnyHosts
                    dryRun = true
                    outputLogLevel = LogLevel.ERROR
                }
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            task(type: SshTask, 'testTask2') {
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            it
        }
    }


    @ConfineMetaClassChanges(Sessions)
    def "task action delegates to executor"() {
        given:
        def sessionsFactory = Mock(Sessions.Factory)
        factoryOf(Sessions) << sessionsFactory
        def sessions1 = Mock(Sessions)
        def sessions2 = Mock(Sessions)

        when:
        def project = project()
        def task1 = project.tasks.testTask1 as SshTask
        def task2 = project.tasks.testTask2 as SshTask

        then: 1 * sessionsFactory.create() >> sessions1
        then: 1 * sessions1.add(_, _)
        then: 1 * sessionsFactory.create() >> sessions2
        then: 1 * sessions2.add(_, _)

        when:
        task1.perform()

        then:
        1 * sessions1.execute(
                ConnectionSettings.DEFAULT + new ConnectionSettings(knownHosts: ConnectionSettings.allowAnyHosts),
                OperationSettings.DEFAULT + new OperationSettings(dryRun: true, outputLogLevel: LogLevel.ERROR)
        )

        when:
        task2.perform()

        then:
        1 * sessions2.execute(ConnectionSettings.DEFAULT, OperationSettings.DEFAULT)
    }

}
