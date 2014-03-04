package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
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
        def globalSpec = project.convention.plugins.ssh.sshSettings

        then:
        globalSpec.knownHosts == SshSettings.allowAnyHosts
        project.remotes.size() == 4
    }

    @Unroll("Filter remotes by roles: #roles")
    def "filter remotes by role"() {
        given:
        def project = createProject()

        when:
        Collection<Remote> remotes = project.remotes.role(roles)

        then:
        contains(remotes, expectedRemoteNames)

        where:
        roles                                | expectedRemoteNames
        'serversA'                           | ['webServer', 'managementServer']
        'noSuchRole'                         | []
        ['serversA', 'serversB'] as String[] | ['webServer', 'appServer', 'managementServer']
    }

    private def contains(Collection<Remote> remotes, List<String> expectedNames) {
        assert remotes.size() == expectedNames.size()

        expectedNames.each { name ->
            assert remotes.find { it.name == name }, "Expected remote: $name not found in remotes"
        }


        return true
    }










    private Project createProject() {
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
