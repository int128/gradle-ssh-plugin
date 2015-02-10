package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.server.SshServerMock.commandWithExit

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class ShellExecutionSpec extends Specification {

    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }
        server.shellFactory = Mock(Factory)
        server.start()

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

    def cleanup() {
        server.stop(true)
    }


    def "it should success if the shell exits with zero status"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(interaction: {})
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(0)
    }

    def "it should throw an exception if the shell exits with non zero status"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(interaction: {})
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(1)

        then:
        BadExitStatusException e = thrown()
        e.exitStatus == 1
    }

    def "it should ignore the exit status if ignoreError is given"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell interaction: {}, ignoreError: true
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(1)
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "shell should write output to logger: #description"() {
        given:
        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(interaction: {})
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(0, outputValue)

        then:
        logMessages.each {
            1 * logger.info("testServer|$it")
        }

        where:
        description            | outputValue                  | logMessages
        'a line'               | 'some result'                | ['some result']
        'a line with line sep' | 'some result\n'              | ['some result']
        'lines'                | 'some result\nsecond line'   | ['some result', 'second line']
        'lines with line sep'  | 'some result\nsecond line\n' | ['some result', 'second line']
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "shell should write output to #logging"() {
        given:
        def out = System.out
        System.out = Mock(PrintStream)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell logging: logging
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(0, 'some message')

        then:
        stdout * System.out.println('testServer|some message')
        slf4j * logger.info('testServer|some message')

        cleanup:
        System.out = out

        where:
        logging        | stdout | slf4j
        LoggingMethod.stdout | 1      | 0
        LoggingMethod.slf4j  | 0      | 1
        LoggingMethod.none   | 0      | 0
    }

    def "shell should write output to file"() {
        given:
        def logFile = temporaryFolder.newFile()

        when:
        logFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.testServer) {
                    shell outputStream: stream
                }
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(0, 'some message')

        then:
        logFile.text == 'some message'
    }

    def "shell can write output to system.out"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell outputStream: System.out
            }
        }

        then:
        1 * server.shellFactory.create() >> commandWithExit(0, 'some message')
    }

}
