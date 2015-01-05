package org.hidetake.gradle.ssh.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.ConnectionSettings
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import spock.lang.Specification
import spock.lang.Unroll

class SshPluginSpec extends Specification {

    def "default settings should be set"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'org.hidetake.ssh'

        then:
        project.ssh.settings instanceof CompositeSettings
        project.ssh.settings.logging == OperationSettings.Logging.stdout
        project.remotes.size() == 0
        project.proxies.size() == 0
        project.SshTask == SshTask
    }

    def "global settings should overwrite default settings"() {
        given:
        def project = ProjectBuilder.builder().build()
        def globalProxy = new Proxy('globalProxy')

        when:
        project.with {
            apply plugin: 'org.hidetake.ssh'

            ssh.settings {
                dryRun = true
                retryCount = 1
                retryWaitSec = 1
                logging = 'slf4j'
                proxy = globalProxy
            }
        }

        then:
        project.ssh.settings.dryRun
        project.ssh.settings.retryCount == 1
        project.ssh.settings.retryWaitSec == 1
        project.ssh.settings.logging == OperationSettings.Logging.slf4j
        project.ssh.settings.proxy == globalProxy
    }

    def "settings, remotes and proxies can be set by configuration closure"() {
        when:
        def project = createProject()

        then:
        project.ssh.settings.knownHosts == ConnectionSettings.Constants.allowAnyHosts
        project.remotes.size() == 4
        project.proxies.size() == 2
    }

    @Unroll
    def "remotes.role() should filter remotes by #roles"() {
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
    def "remote #remoteName should be configured with proxy #expectedProxyName"() {
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
            apply plugin: 'org.hidetake.ssh'
            remotes {
                webServer {}
            }
        }

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'org.hidetake.ssh'
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
            apply plugin: 'org.hidetake.ssh'
            remotes {
                webServer { role 'roleA' }
            }
        }

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'org.hidetake.ssh'
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
            apply plugin: 'org.hidetake.ssh'
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
            apply plugin: 'org.hidetake.ssh'
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
            apply plugin: 'org.hidetake.ssh'
            proxies {
                socks {}
            }
        }

        def childProject = ProjectBuilder.builder().withParent(parentProject).build()
        childProject.with {
            apply plugin: 'org.hidetake.ssh'
            proxies {
                http {}
            }
        }

        then:
        proxyNameSet(parentProject.proxies) == ['socks'].toSet()
        proxyNameSet(childProject.proxies) == ['socks', 'http'].toSet()
    }

    private static createProject() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'org.hidetake.ssh'

            ssh.settings {
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
