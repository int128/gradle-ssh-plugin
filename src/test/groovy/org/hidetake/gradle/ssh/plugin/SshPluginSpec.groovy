package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.SshTaskService
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

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
        project.ssh.knownHosts == ConnectionSettings.Constants.allowAnyHosts
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


    @ConfineMetaClassChanges(SshTaskService)
    def "invoke sshexec"() {
        given:
        def service = Mock(SshTaskService)
        SshTaskService.metaClass.static.getInstance = { -> service }

        def project = createProject()

        when:
        project.with {
            sshexec {
                ssh {
                    knownHosts = file('my_known_hosts')
                }
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
        }

        then: 1 * service.execute(new CompositeSettings(
                connectionSettings: new ConnectionSettings(knownHosts: ConnectionSettings.Constants.allowAnyHosts)
        ), _)
    }

    @ConfineMetaClassChanges(SshTaskService)
    def "sshexec() returns a result of the closure"() {
        given:
        def service = Mock(SshTaskService)
        SshTaskService.metaClass.static.getInstance = { -> service }

        def project = createProject()

        when:
        project.with {
            project.ext.actualResult = sshexec {
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
        }

        then: 1 * service.execute(new CompositeSettings(
                connectionSettings: new ConnectionSettings(knownHosts: ConnectionSettings.Constants.allowAnyHosts)
        ), _) >> 'ls-result'

        then: project.ext.actualResult == 'ls-result'
    }

    def "invoke sshexec with null"() {
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

            it
        }
    }

}
