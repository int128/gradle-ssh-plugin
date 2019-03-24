package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.command.CommandFactory
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command

class ScriptSpec extends Specification {

    @Shared
    SshServer server

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            authenticate('someuser', 'somepassword', _) >> true
        }
        server.start()
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert server.activeSessions.empty
        }
        server.stop()
    }

    def setup() {
        server.commandFactory = Mock(CommandFactory)

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


    def "executeScript should execute a script"() {
        given:
        def actualScript = new ByteArrayOutputStream()

        when:
        def actualResult = ssh.run {
            session(ssh.remotes.testServer) {
                executeScript 'foo!'
            }
        }

        then:
        1 * server.commandFactory.createCommand('/bin/sh') >> command(0) {
            outputStream << 'something'
            actualScript << inputStream
        }

        then:
        actualResult == 'something'
        actualScript.toString() == 'foo!'
    }

    def "executeScript should execute a script with settings"() {
        given:
        def actualScript = new ByteArrayOutputStream()

        when:
        def actualResult = ssh.run {
            session(ssh.remotes.testServer) {
                executeScript 'foo!', ignoreError: true
            }
        }

        then:
        1 * server.commandFactory.createCommand('/bin/sh') >> command(1) {
            outputStream << 'something'
            actualScript << inputStream
        }

        then:
        actualResult == 'something'
        actualScript.toString() == 'foo!'
    }

    def "executeScript should return a result of script via closure"() {
        given:
        def actualScript = new ByteArrayOutputStream()
        def actualResult

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeScript('foo!') { result ->
                    actualResult = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand('/bin/sh') >> command(0) {
            outputStream << 'something'
            actualScript << inputStream
        }

        then:
        actualResult == 'something'
        actualScript.toString() == 'foo!'
    }

    def "executeScript should return a result of script via closure with settings"() {
        given:
        def actualScript = new ByteArrayOutputStream()
        def actualResult

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeScript('foo!', ignoreError: true) { result ->
                    actualResult = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand('/bin/sh') >> command(1) {
            outputStream << 'something'
            actualScript << inputStream
        }

        then:
        actualResult == 'something'
        actualScript.toString() == 'foo!'
    }

    def "executeScript should throw an error if inputStream is set"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeScript 'foo!', inputStream: 'bar!'
            }
        }

        then:
        thrown(IllegalArgumentException)
    }

}
