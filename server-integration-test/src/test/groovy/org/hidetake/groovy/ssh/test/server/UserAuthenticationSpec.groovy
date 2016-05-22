package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification
import spock.lang.Unroll

import java.security.PublicKey

import static SshServerMock.commandWithExit
import static org.hidetake.groovy.ssh.test.server.UserKeyFixture.KeyType
import static org.hidetake.groovy.ssh.test.server.UserKeyFixture.privateKey

class UserAuthenticationSpec extends Specification {

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.commandFactory = Mock(CommandFactory)

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
    }

    def cleanup() {
        server.stop(true)
    }

    def "password authentication should pass if exact one is given"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.start()

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

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "password authentication should fail if wrong one is given"() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'wrongpassword'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.passwordAuthenticator.authenticate('someuser', 'wrongpassword', _) >> false

        then:
        0 * server.commandFactory.createCommand(_)

        and:
        JSchException e = thrown()
        e.message == 'Auth fail'
    }

    @Unroll
    def "public key authentication should pass if valid #keyType #type is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = identitySetting
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate(
                'someuser', { PublicKey k -> k.algorithm == keyType } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)

        where:
        keyType | type      | identitySetting
        'RSA'   | 'File'   | privateKey(KeyType.rsa)
        'RSA'   | 'String' | privateKey(KeyType.rsa).text
        'EC'    | 'File'   | privateKey(KeyType.ecdsa)
        'EC'    | 'String' | privateKey(KeyType.ecdsa).text
    }

    @Unroll
    def "public key authentication should pass if valid #keyType #type is given in global settings"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

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
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate(
                'someuser', { PublicKey k -> k.algorithm == keyType } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)

        where:
        keyType | type      | identitySetting
        'RSA'   | 'File'   | privateKey(KeyType.rsa)
        'RSA'   | 'String' | privateKey(KeyType.rsa).text
        'EC'    | 'File'   | privateKey(KeyType.ecdsa)
        'EC'    | 'String' | privateKey(KeyType.ecdsa).text
    }

    @Unroll
    def "public key authentication should fail if wrong key #type is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey()
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', _ as PublicKey, _) >> false

        then:
        0 * server.commandFactory.createCommand(_)

        and:
        JSchException e = thrown()
        e.message == 'Auth fail'

        where:
        type      | identitySetting
        'File'   | privateKey()
        'String' | privateKey().text
    }

    def "public key authentication should accept the passphrase of identity"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey(KeyType.rsa_pass)
                passphrase = "gradle"
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    @Unroll
    def "authentication methods can be set as #userAuthenticationMethods"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.start()

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
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        pk * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true
        pw * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)

        where:
        userAuthenticationMethods   | pw    | pk
        ['publickey']               | 0     | 2
        ['password']                | 1     | 0
        ['publickey', 'password']   | 0     | 2
        ['password', 'publickey']   | 1     | 0
    }

    def "JSchException should be thrown if no authentication method is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

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
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        thrown(JSchException)
    }

    def "JSchException should be thrown if an invalid authentication method is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

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
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        thrown(JSchException)
    }

    def "identity and passphrase can be set by global settings"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

        ssh.settings {
            identity = privateKey(KeyType.rsa_pass)
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
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        1 * server.commandFactory.createCommand('ls') >> commandWithExit(0)
    }

    def "public key authentication should fail if wrong passphrase is given"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey(KeyType.rsa_pass)
                passphrase = "wrong"
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        0 * server.commandFactory.createCommand(_)

        and:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

    def "remote specific identity should precede one in global settings"() {
        given:
        server.publickeyAuthenticator = Mock(PublickeyAuthenticator)
        server.start()

        ssh.settings {
            identity = privateKey(KeyType.rsa)
        }

        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                identity = privateKey(KeyType.rsa_pass)
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'ls'
            }
        }

        then:
        (1.._) * server.publickeyAuthenticator.authenticate('someuser', { PublicKey k -> k.algorithm == 'RSA' } as PublicKey, _) >> true

        then:
        0 * server.commandFactory.createCommand(_)

        then:
        JSchException e = thrown()
        e.message == 'USERAUTH fail'
    }

}
