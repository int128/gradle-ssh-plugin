package org.hidetake.gradle.ssh.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings
import spock.lang.Specification
import spock.lang.Unroll

class SshPluginSpec extends Specification {

    def "apply with no config just adds plugin"() {
        given:
        def project = ProjectBuilder.builder().build()
        def plugin = new SshPlugin()


        when:
        plugin.apply(project)

        then:
        project.container(Remote).size() == 0
        project.convention.plugins.ssh
    }

    def "apply the full monty"() {
        when:
        def project = createProject()
        def globalSettings = project.convention.getPlugin(SshPluginConvention).globalSettings

        then:
        globalSettings.knownHosts == ConnectionSettings.allowAnyHosts
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
