package org.hidetake.groovy.ssh

import ch.qos.logback.classic.Level
import org.apache.sshd.SshServer
import org.apache.sshd.common.keyprovider.FileKeyPairProvider
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.operation.Command
import org.hidetake.groovy.ssh.test.server.FilenameUtils
import org.hidetake.groovy.ssh.test.server.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static FilenameUtils.toUnixSeparator
import static SshServerMock.commandWithExit

class MainSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "main should show usage if no arg is given"() {
        given:
        def stdout = System.out
        def stdoutBuffer = new ByteArrayOutputStream()
        System.out = new PrintStream(stdoutBuffer)

        when:
        Main.main()

        then:
        stdoutBuffer.toString('UTF-8').contains('usage:')

        cleanup:
        System.out = stdout
    }

    def "main should show version if --version is given"() {
        given:
        def stdout = System.out
        def stdoutBuffer = new ByteArrayOutputStream()
        System.out = new PrintStream(stdoutBuffer)

        when:
        Main.main '--version'

        then:
        stdoutBuffer.toString('UTF-8').trim() == Ssh.release.toString()

        cleanup:
        System.out = stdout
    }


    @ConfineMetaClassChanges(Command)
    def "main should read script from a file is path is given"() {
        given:
        def server = createServer()
        def script = createScript(server)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        def scriptFile = temporaryFolder.newFile()
        scriptFile << script

        when:
        Main.main scriptFile.path

        then:
        1 * logger.info ({ it =~ /Remote\d+?#\d+?\|some message/ })
        1 * logger.error({ it =~ /Remote\d+?#\d+?\|error/})

        cleanup:
        server.stop()
    }

    @ConfineMetaClassChanges(Command)
    def "main should evaluate a script line if -e is given"() {
        given:
        def server = createServer()
        def script = createScript(server)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        when:
        Main.main '-e', script

        then:
        1 * logger.info ({ it =~ /Remote\d+?#\d+?\|some message/ })
        1 * logger.error({ it =~ /Remote\d+?#\d+?\|error/})

        cleanup:
        server.stop()
    }

    @ConfineMetaClassChanges(Command)
    def "main should read script from standard input if --stdin is given"() {
        given:
        def server = createServer()
        def script = createScript(server)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        def stdin = System.in
        System.in = new ByteArrayInputStream(script.bytes)

        when:
        Main.main '--stdin'

        then:
        1 * logger.info ({ it =~ /Remote\d+?#\d+?\|some message/ })
        1 * logger.error({ it =~ /Remote\d+?#\d+?\|error/})

        cleanup:
        System.in = stdin
        server.stop()
    }


    def "default log level should be INFO"() {
        given:
        def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        root.setLevel(logbackLogLevel('ERROR'))

        when:
        Main.main()

        then:
        root.getLevel() == logbackLogLevel('INFO')
    }

    @Unroll
    def "flag #flags should set log level to #level"() {
        given:
        def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        root.setLevel(logbackLogLevel('ERROR'))

        when:
        Main.main(flags.toArray(new String[0]))

        then:
        root.getLevel() == logbackLogLevel(level)

        where:
        flags               | level
        ['-q']              | 'WARN'
        ['-i']              | 'INFO'
        ['-q', '-i']        | 'INFO'
        ['-d']              | 'DEBUG'
        ['-i', '-d']        | 'DEBUG'
        ['-q', '-d', '-i']  | 'DEBUG'
    }

    def "log level can be set by logback method in the script"() {
        given:
        def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        assert root instanceof ch.qos.logback.classic.Logger

        when:
        Main.main '-e', '''
            ssh.runtime.logback level: 'ERROR'
        '''

        then:
        root.level == Level.ERROR
    }


    private SshServer createServer() {
        def privateKey = MainSpec.getResource('/hostkey_dsa').file
        def server = SshServerMock.setUpLocalhostServer(new FileKeyPairProvider(privateKey))
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        server.commandFactory = Mock(CommandFactory)
        server.commandFactory.createCommand('somecommand') >> commandWithExit(0, 'some message', 'error')
        server.start()
        server
    }

    private String createScript(SshServer server) {
        def publicKey = MainSpec.getResourceAsStream('/hostkey_dsa.pub').text
        def knownHostsFile = temporaryFolder.newFile() << "[localhost]:${server.port} ${publicKey}"
        "ssh.run {" +
                " session(host: 'localhost'," +
                "  port: ${server.port}," +
                "  knownHosts: new File('${toUnixSeparator(knownHostsFile.path)}')," +
                "  user: 'someuser'," +
                "  password: 'somepassword')" +
                " { execute('somecommand') }" +
                "}"
    }


    private static final logbackLogLevel(String level) {
        Class.forName('ch.qos.logback.classic.Level').toLevel(level)
    }

}
