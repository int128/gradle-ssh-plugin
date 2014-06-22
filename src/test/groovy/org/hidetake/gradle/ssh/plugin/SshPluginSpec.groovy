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
        project.proxies.size() == 0
        project.SshTask == SshTask
    }


    def "apply global settings"() {
        given:
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'ssh'
        def globalProxy = new Proxy('globalProxy')

        when:
        project.ssh {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
            proxy = globalProxy
        }

        then:
        project.ssh.dryRun
        project.ssh.retryCount == 1
        project.ssh.retryWaitSec == 1
        project.ssh.outputLogLevel == LogLevel.DEBUG
        project.ssh.errorLogLevel == LogLevel.INFO
        project.ssh.proxy == globalProxy
    }

    def "apply the full monty"() {
        when:
        def project = createProject()

        then:
        project.ssh.knownHosts == ConnectionSettings.Constants.allowAnyHosts
        project.remotes.size() == 4
        project.proxies.size() == 2
    }

    @Unroll
    def "filter remotes by role: #roles"() {
        given:
        def project = createProject()

        when:
        def actualRemoteNames = remoteNameSet(project.remotes.role(roles))

        then:
        actualRemoteNames.toSet() == expectedRemoteNames.toSet()

        where:
        roles                                | expectedRemoteNames
        'noSuchRole'                         | []
        'serversA'                           | ['webServer', 'managementServer']
        'serversB'                           | ['appServer', 'managementServer']
        ['serversA', 'serversB'] as String[] | ['webServer', 'appServer', 'managementServer']
    }

    @Unroll
    def "remote #remoteName is configured with expected proxy"() {
        given:
        def project = createProject()

        when:
        def associated = project.remotes[remoteName]
        def actualProxyName = associated.proxy?.name
        
        then:
        actualProxyName == expectedProxyName
        
        where:
        remoteName          | expectedProxyName
        'webServer'         | null
        'appServer'         | 'socks'
        'dbServer'          | null
        'managementServer'  | 'http'
    }

    def "remotes on the parent project should be inherited to children"() {
        when:
        def parentProject = ProjectBuilder.builder().build()
        parentProject.with {
            apply plugin: 'ssh'
            remotes {
                webServer {}
            }
        }

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'ssh'
            remotes {
                appServer {}
            }
        }

        then:
        remoteNameSet(parentProject.remotes) == ['webServer'].toSet()
        remoteNameSet(childProject.remotes) == ['webServer', 'appServer'].toSet()
    }

    def "role can be applied for remotes on the parent project"() {
        when:
        def parentProject = ProjectBuilder.builder().build()
        parentProject.with {
            apply plugin: 'ssh'
            remotes {
                webServer { role 'roleA' }
            }
        }

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'ssh'
            remotes {
                appServer { role 'roleB' }
            }
        }

        then:
        remoteNameSet(childProject.remotes.role('roleA')) == ['webServer'].toSet()
        remoteNameSet(childProject.remotes.role('roleB')) == ['appServer'].toSet()
    }

    def "the parent project without the plugin is ignored"() {
        when:
        def parentProject = ProjectBuilder.builder().build()

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'ssh'
            remotes {
                appServer {}
            }
        }

        then:
        remoteNameSet(childProject.remotes) == ['appServer'].toSet()
    }

    def "the child project without the plugin is ignored"() {
        when:
        def parentProject = ProjectBuilder.builder().build()
        parentProject.with {
            apply plugin: 'ssh'
            remotes {
                webServer {}
            }
        }

        and: 'create the child project'
        ProjectBuilder.builder().withParent(parentProject).build()

        then:
        remoteNameSet(parentProject.remotes) == ['webServer'].toSet()
    }

    def "proxies on the parent project should be inherited to children"() {
        when:
        def parentProject = ProjectBuilder.builder().build()
        parentProject.with {
            apply plugin: 'ssh'
            proxies {
                socks {}
            }
        }

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'ssh'
            proxies {
                http {}
            }
        }

        then:
        proxyNameSet(parentProject.proxies) == ['socks'].toSet()
        proxyNameSet(childProject.proxies) == ['socks', 'http'].toSet()
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

            proxies {
                http { type = HTTP }
                socks { type = SOCKS }
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
                    proxy = proxies.socks
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
                    proxy = proxies.http
                }
            }

            it
        }
    }

    private static remoteNameSet(Collection<Remote> remotes) {
        remotes.collect { it.name }.toSet()
    }

    private static proxyNameSet(Collection<Proxy> proxies) {
        proxies.collect { it.name }.toSet()
    }

}
