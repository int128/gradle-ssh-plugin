package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

@Timeout(10)
class RetrySpec extends Specification {

    @Shared
    SshServer server

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
    }

    def setup() {
        server.passwordAuthenticator = Mock(PasswordAuthenticator)

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }
    }


    def 'should retry but fail'() {
        given:
        ssh.settings {
            retryWaitSec = 1
            retryCount = 1
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {}
        }

        then: 'connection refused'
        JSchException e = thrown()
        e.cause instanceof ConnectException
    }

    def 'should retry and success'() {
        given:
        ssh.settings {
            retryWaitSec = 2
            retryCount = 1
        }

        and: 'start SSH server after 1 second'
        def thread = Thread.start {
            Thread.sleep(1000L)
            server.start()
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {}
        }

        then:
        (1.._) * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true

        cleanup:
        thread.join()
        server.stop()
    }

}
