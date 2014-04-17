package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Sessions
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
            task(type: SshTask, 'testTask') {
                dryRun = true
                outputLogLevel = LogLevel.ERROR
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
        def sessions = Mock(Sessions)
        factoryOf(Sessions) << Mock(Sessions.Factory) {
            create() >> sessions
        }

        when:
        def project = project()
        def task = project.tasks.testTask as SshTask

        then:
        1 * sessions.add(_, _)

        when:
        task.perform()

        then:
        1 * sessions.execute(_) >> { SshSettings settings ->
            assert settings.dryRun
            assert settings.outputLogLevel == LogLevel.ERROR
        }
    }

}
