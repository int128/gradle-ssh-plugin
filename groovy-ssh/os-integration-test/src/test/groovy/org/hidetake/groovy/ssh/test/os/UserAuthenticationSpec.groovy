package org.hidetake.groovy.ssh.test.os


import com.jcraft.jsch.JSchException
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Timeout

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes
import static org.hidetake.groovy.ssh.test.os.Fixture.randomInt

/**
 * Check if user authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
@Timeout(10)
class UserAuthenticationSpec extends Specification {

    private static final user1 = "groovyssh${randomInt()}"

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    @Rule
    SshAgent sshAgent

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
        ssh.settings.extensions.add(UserManagementExtension)
    }

    def 'should authenticate by ECDSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.DefaultWithECDSAKey) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should authenticate by pass-phrased RSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.DefaultWithPassphrase) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should fail if agent is not available'() {
        when:
        ssh.run {
            session(ssh.remotes.DefaultWithAgent) {
                execute "id"
            }
        }

        then:
        thrown(JSchException)
    }

    def 'should authenticate with SSH agent'() {
        given:
        sshAgent.add(ssh.remotes.Default.identity.path)

        and:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.DefaultWithAgent) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'login to localhost should fail if agent forwarding is disabled'() {
        given:
        sshAgent.add(ssh.remotes.Default.identity.path)

        when:
        ssh.run {
            session(ssh.remotes.DefaultWithAgent) {
                execute "ssh -o StrictHostKeyChecking=no localhost id"
            }
        }

        then:
        thrown(BadExitStatusException)
    }

    def 'login to localhost should succeed if agent forwarding is enabled'() {
        given:
        sshAgent.add(ssh.remotes.Default.identity.path)

        when:
        def id = ssh.run {
            session(ssh.remotes.DefaultWithAgent) {
                execute "ssh -o StrictHostKeyChecking=no localhost id", agentForwarding: true
            }
        }

        then:
        id.contains(ssh.remotes.DefaultWithAgent.user)
    }

}
