package org.hidetake.groovy.ssh.api

import org.hidetake.groovy.ssh.internal.DefaultRunHandler
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.Ssh.ssh

class ServiceSpec extends Specification {

    def cleanup() {
        ssh.remotes.clear()
        ssh.proxies.clear()
        ssh.settings.reset()
    }

    def "global settings can be set"() {
        given:
        ssh.proxies {
            globalProxy {}
        }

        when:
        ssh.settings {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            proxy = ssh.proxies.globalProxy
        }

        then:
        ssh.settings.dryRun
        ssh.settings.retryCount == 1
        ssh.settings.retryWaitSec == 1
        ssh.settings.proxy == ssh.proxies.globalProxy
    }

    @Unroll
    def "filter remotes by role: #roles"() {
        given:
        configureFixture()

        when:
        def actualRemoteNames = remoteNameSet(ssh.remotes.role(roles))

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
        configureFixture()

        when:
        def associated = ssh.remotes[remoteName]
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

    @ConfineMetaClassChanges(DefaultRunHandler)
    def "ssh.run() should call internal service"() {
        given:
        def called = Mock(Closure)
        DefaultRunHandler.metaClass.run = { CompositeSettings s -> called(s) }

        configureFixture()

        ssh.settings {
            knownHosts = allowAnyHosts
        }

        when:
        ssh.run {
            session(ssh.remotes.webServer) {
                execute 'ls'
            }
        }

        then: 1 * called(new CompositeSettings(
            connectionSettings: new ConnectionSettings(knownHosts: ConnectionSettings.Constants.allowAnyHosts)
        ))
    }

    @ConfineMetaClassChanges(DefaultRunHandler)
    def "ssh.run() should return result of the closure"() {
        given:
        def called = Mock(Closure)
        DefaultRunHandler.metaClass.run = { CompositeSettings s -> called(s) }

        configureFixture()

        ssh.settings {
            knownHosts = allowAnyHosts
        }

        when:
        def actualResult = ssh.run {
            session(ssh.remotes.webServer) {
                execute 'ls'
            }
        }

        then: 1 * called(new CompositeSettings(
            connectionSettings: new ConnectionSettings(knownHosts: ConnectionSettings.Constants.allowAnyHosts)
        )) >> 'ls-result'

        then: actualResult == 'ls-result'
    }

    def "ssh.run(null) causes error"() {
        when:
        ssh.run(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }

    private static void configureFixture() {
        ssh.settings {
            knownHosts = allowAnyHosts
        }

        ssh.proxies {
            http { type = HTTP }
            socks { type = SOCKS }
        }

        ssh.remotes {
            webServer {
                role 'serversA'
                host = 'web'
                user = 'webuser'
            }
            appServer {
                role 'serversB'
                host = 'app'
                user = 'appuser'
                proxy = ssh.proxies.socks
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
                proxy = ssh.proxies.http
            }
        }
    }

    private static remoteNameSet(Collection<Remote> remotes) {
        remotes.collect { it.name }.toSet()
    }

}
