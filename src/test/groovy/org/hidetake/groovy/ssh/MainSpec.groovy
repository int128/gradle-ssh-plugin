package org.hidetake.groovy.ssh

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.server.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.server.SshServerMock.commandWithExit

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
        stdoutBuffer.toString('UTF-8').trim() == "${Ssh.product.name}-${Ssh.product.version}"

        cleanup:
        System.out = stdout
    }


    @ConfineMetaClassChanges(DefaultOperations)
    def "main should read script from a file is path is given"() {
        given:
        def server = createServer()
        def script = createScript(server)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        def scriptFile = temporaryFolder.newFile()
        scriptFile << script

        when:
        Main.main scriptFile.path

        then:
        1 * logger.info ('localhost|some message')
        1 * logger.error('localhost|error')

        cleanup:
        server.stop()
    }

    @ConfineMetaClassChanges(DefaultOperations)
    def "main should evaluate a script line if -e is given"() {
        given:
        def server = createServer()
        def script = createScript(server)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        when:
        Main.main '-e', script

        then:
        1 * logger.info ('localhost|some message')
        1 * logger.error('localhost|error')

        cleanup:
        server.stop()
    }

    @ConfineMetaClassChanges(DefaultOperations)
    def "main should read script from standard input if --stdin is given"() {
        given:
        def server = createServer()
        def script = createScript(server)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        def stdin = System.in
        System.in = new ByteArrayInputStream(script.bytes)

        when:
        Main.main '--stdin'

        then:
        1 * logger.info ('localhost|some message')
        1 * logger.error('localhost|error')

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


    private SshServer createServer() {
        def server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        server.commandFactory = Mock(CommandFactory)
        server.commandFactory.createCommand('somecommand') >> commandWithExit(0, 'some message', 'error')
        server.start()
        server
    }

    private String createScript(SshServer server) {
        def hostKey = MainSpec.getResourceAsStream('/hostkey.pub').text
        def knownHostsFile = temporaryFolder.newFile() << "[localhost]:${server.port} ${hostKey}"
        "ssh.run {" +
                " session(host: 'localhost'," +
                "  port: ${server.port}," +
                "  knownHosts: new File('${knownHostsFile.path}')," +
                "  user: 'someuser'," +
                "  password: 'somepassword')" +
                " { execute('somecommand') }" +
                "}"
    }


    private static final logbackLogLevel(String level) {
        Class.forName('ch.qos.logback.classic.Level').toLevel(level)
    }

}
