package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.session.Sessions
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.test.RegistryHelper.factoryOf

class SshPluginSpec extends Specification {

    def "apply the plugin"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'ssh'

        then:
        project.ssh
        project.remotes.size() == 0
        project.SshTask == SshTask
    }


    def "apply global settings"() {
        given:
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'ssh'

        when:
        project.ssh {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
        }

        then:
        project.ssh.dryRun
        project.ssh.retryCount == 1
        project.ssh.retryWaitSec == 1
        project.ssh.outputLogLevel == LogLevel.DEBUG
        project.ssh.errorLogLevel == LogLevel.INFO
    }

    def "apply the full monty"() {
        when:
        def project = createProject()

        then:
        project.ssh.knownHosts == ConnectionSettings.allowAnyHosts
        project.remotes.size() == 4
    }

    @Unroll
    def "filter remotes by role: #roles"() {
        given:
        def project = createProject()

        when:
        Collection<Remote> associated = project.remotes.role(roles)
        def actualRemoteNames = associated.collect { it.name }

        then:
        actualRemoteNames.toSet() == expectedRemoteNames.toSet()

        where:
        roles                                | expectedRemoteNames
        'noSuchRole'                         | []
        'serversA'                           | ['webServer', 'managementServer']
        'serversB'                           | ['appServer', 'managementServer']
        ['serversA', 'serversB'] as String[] | ['webServer', 'appServer', 'managementServer']
    }


    @ConfineMetaClassChanges(Sessions)
    def "apply task specific settings"() {
        given:
        def project = createProject()
        def sessionsFactory = Mock(Sessions.Factory)
        factoryOf(Sessions) << sessionsFactory
        def sessions = Mock(Sessions)
        sessionsFactory.create() >> sessions

        when:
        project.with {
            sshexec {
                ssh {
                    retryCount = 100
                    outputLogLevel = LogLevel.ERROR
                }
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
        }

        then: 1 * sessions.add(project.remotes.webServer, _)
        then: 1 * sessions.execute(
                ConnectionSettings.DEFAULT + new ConnectionSettings(retryCount: 100, knownHosts: ConnectionSettings.allowAnyHosts),
                OperationSettings.DEFAULT + new OperationSettings(outputLogLevel: LogLevel.ERROR)
        )

        when:
        project.with {
            sshexec {
                session(remotes.appServer) {
                    execute 'ls'
                }
            }
        }

        then: 1 * sessions.add(project.remotes.appServer, _)
        then: 1 * sessions.execute(
                ConnectionSettings.DEFAULT + new ConnectionSettings(knownHosts: ConnectionSettings.allowAnyHosts),
                OperationSettings.DEFAULT
        )
    }

    def "apply task specific settings but null"() {
        given:
        def project = createProject()

        when:
        project.sshexec(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }



    private static createProject() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'ssh'

            ssh {
                knownHosts = allowAnyHosts
            }

            remotes {
                webServer {
                    role 'serversA'
                    host = 'web'
                    user = 'webuser'
                }
                appServer {
                    role 'serversB'
                    host = 'app'
                    user = 'appuser'
                }
                dbServer {
                    host = 'db'
                    user = 'dbuser'
                }
                managementServer {
                    role 'serversA'
                    role 'serversB'
                    host = 'mng'
                    user = 'mnguser'
                }
            }

            task(type: SshTask, 'testTask') {
                session(remotes.webServer) {
                    execute "ls -l"
                }
            }

            it
        }
    }

}
