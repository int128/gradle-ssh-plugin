package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
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
        ssh.remotes {
            InternalServer {
                host = 'groovy-ssh-integration-test-internal-box'
                user = ssh.remotes.Default.user
                identity = ssh.remotes.Default.identity
                gateway = ssh.remotes.Default
            }
        }
    }

    //FIXME: at this time no way to test multi-hop on CircleCI
    @Ignore
    def "it should connect to target server via gateway server"() {
        given:
        def knownHostsFile = temporaryFolder.newFile()

        when:
        ssh.run {
            settings {
                knownHosts = addHostKey(knownHostsFile)
            }
            session(ssh.remotes.InternalServer) {
                execute 'hostname'
            }
        }

        then:
        knownHostsFile.text =~ /^groovy-ssh-integration-test-internal-box .+/

        when:
        ssh.run {
            settings {
                knownHosts = knownHostsFile
            }
            session(ssh.remotes.InternalServer) {
                execute 'hostname'
            }
        }

        then:
        knownHostsFile.text =~ /^groovy-ssh-integration-test-internal-box .+/
    }

}
