package org.hidetake.groovy.ssh.core

import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.internal.session.Plan
import org.hidetake.groovy.ssh.internal.session.SessionExecutor
import spock.lang.Specification
import spock.lang.Unroll

class ServiceSpec extends Specification {

    Service ssh
    SessionExecutor sessionExecutor

    def setup() {
        sessionExecutor = Mock(SessionExecutor)
        ssh = new Service(sessionExecutor)
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

    def "logging setting can be set by a string"() {
        when:
        ssh.settings {
            logging = 'stdout'
        }

        then:
        ssh.settings.logging == OperationSettings.Logging.stdout
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

    def "ssh.run() should call executor and return last result"() {
        given:
        def remote1 = new Remote('myRemote1')
        remote1.user = 'myUser1'
        remote1.host = 'myHost1'
        def remote2 = new Remote('myRemote2')
        remote2.user = 'myUser2'
        remote2.host = 'myHost2'
        def closure = {
            execute 'something'
        }

        when:
        def result = ssh.run {
            session(remote1, remote2, closure)
        }

        then:
        1 * sessionExecutor.execute(_, [new Plan(remote1, closure), new Plan(remote2, closure)]) >> ['result1', 'result2']

        then:
        result == 'result2'
    }

    def "ssh.run(null) causes error"() {
        when:
        ssh.run(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }

    private void configureFixture() {
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
