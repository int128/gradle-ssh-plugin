package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes

/**
 * Check if gateway access should work with Linux system.
 *
 * @author Hidetake Iwata
 */
class GatewaySpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    def "it should connect to target server via gateway server"() {
        given:
        def knownHostsOfGateway = ssh.remotes.Default.knownHosts as File
        def knownHostsOfTarget = temporaryFolder.newFile()
        knownHostsOfGateway.eachLine { line ->
            knownHostsOfTarget << line.replaceAll(/^[^ ]+/, '127.0.0.2') << '\n'
        }

        ssh.remotes {
            TargetServer {
                host = '127.0.0.2'
                knownHosts = knownHostsOfTarget
                user = ssh.remotes.Default.user
                identity = ssh.remotes.Default.identity
                gateway = ssh.remotes.Default
            }
        }

        when:
        def from = ssh.run {
            session(ssh.remotes.TargetServer) {
                execute 'echo ${SSH_CLIENT%% *}'
            }
        }

        then:
        from == '127.0.0.1'
    }

}
