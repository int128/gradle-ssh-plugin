package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
import org.apache.sshd.server.command.CommandFactory
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.security.PublicKey

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command
import static org.hidetake.groovy.ssh.test.server.UserKeyFixture.KeyType
import static org.hidetake.groovy.ssh.test.server.UserKeyFixture.privateKey

class UserAuthenticationSpec extends Specification {

    @Shared
    SshServer server

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.commandFactory = Mock(CommandFactory) {
            createCommand('ls') >> command(0)
        }
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert server.activeSessions.empty
        }
        server.stop()
    }

    def setup() {
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
    }


    def "password authentication should pass if exact one is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
    }

    def "password authentication should fail if wrong one is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'wrongpassword'
            }
        }

        when:
        executeCommand()

        then:
        (1.._) * server.passwordAuthenticator.authenticate('someuser', 'wrongpassword', _) >> false

        and:
        JSchException e = thrown()
        e.message =~ /^Auth fail/
    }

    @Unroll
    def "public key authentication should pass if valid #keyType #type is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identitySetting
            }
        }

        when:
        executeCommand()

        then:
        (1.._) * server.publickeyAuthenticator.authenticate(
                'someuser', { PublicKey k -> k.algorithm == keyType } as PublicKey, _) >> true

        where:
        keyType | type      | identitySetting
        'EC'    | 'File'   | privateKey(KeyType.ecdsa)
        'EC'    | 'String' | privateKey(KeyType.ecdsa).text
    }

    @Unroll
    def "public key authentication should pass if valid #keyType #type is given in global settings"() {
        given:
        ssh.settings {
            identity = identitySetting
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
            }
        }

        when:
        executeCommand()

        then:
        (1.._) * server.publickeyAuthenticator.authenticate(
                'someuser', { PublicKey k -> k.algorithm == keyType } as PublicKey, _) >> true

        where:
        keyType | type      | identitySetting
        'EC'    | 'File'   | privateKey(KeyType.ecdsa)
        'EC'    | 'String' | privateKey(KeyType.ecdsa).text
    }

    @Unroll
    def "public key authentication should fail if wrong key #type is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey()
            }
        }

        when:
        executeCommand()

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', _ as PublicKey, _) >> false

        and:
        JSchException e = thrown()
        e.message =~ /^Auth fail/

        where:
        type        | identitySetting
        'File'      | privateKey()
        'String'    | privateKey().text
    }

    def "public key authentication should accept the passphrase of identity"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey(KeyType.ecdsa_pass)
                passphrase = "gradle"
            }
        }

        when:
        executeCommand()

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'EC' } as PublicKey, _) >> true
    }

    @Unroll
    def "authentication methods can be set as #userAuthenticationMethods"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
                identity = privateKey()
                authentications = userAuthenticationMethods
            }
        }

        when:
        executeCommand()

        then:
        pk * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'EC' } as PublicKey, _) >> true
        pw * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true

        where:
        userAuthenticationMethods   | pw    | pk
        ['publickey']               | 0     | 2
        ['password']                | 1     | 0
        ['publickey', 'password']   | 0     | 2
        ['password', 'publickey']   | 1     | 0
    }

    def "JSchException should be thrown if no authentication method is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
                identity = privateKey()
                authentications = []
            }
        }

        when:
        executeCommand()

        then:
        thrown(JSchException)
    }

    def "JSchException should be thrown if an invalid authentication method is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
                identity = privateKey()
                authentications = ['invalid']
            }
        }

        when:
        executeCommand()

        then:
        thrown(JSchException)
    }

    def "identity and passphrase can be set by global settings"() {
        given:
        ssh.settings {
            identity = privateKey(KeyType.ecdsa_pass)
            passphrase = "gradle"
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
            }
        }

        when:
        executeCommand()

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'EC' } as PublicKey, _) >> true
    }

    def "public key authentication should fail if wrong passphrase is given"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey(KeyType.ecdsa_pass)
                passphrase = "wrong"
            }
        }

        when:
        executeCommand()

        then:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

    def "remote specific identity should precede one in global settings"() {
        given:
        ssh.settings {
            identity = privateKey(KeyType.ecdsa)
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey(KeyType.ecdsa_pass)
            }
        }

        when:
        executeCommand()

        then:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }


    private executeCommand() {
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }
    }

}
