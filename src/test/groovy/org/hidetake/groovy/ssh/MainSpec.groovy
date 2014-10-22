package org.hidetake.groovy.ssh

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.internal.operation.DefaultOperations
import org.hidetake.groovy.ssh.server.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(DefaultOperations)
class MainSpec extends Specification {

    SshServer server

    Logger logger

    String script

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.errorStream.withWriter('UTF-8') { it << 'error' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        def hostKey = MainSpec.getResourceAsStream('/hostkey.pub').text
        def knownHostsFile = temporaryFolder.newFile() << "[localhost]:${server.port} ${hostKey}"
        script = "ssh.run {" +
                "session(host: 'localhost'," +
                " port: ${server.port}," +
                " knownHosts: new File('${knownHostsFile.path}')," +
                " user: 'someuser'," +
                " password: 'somepassword')" +
                "{ execute('somecommand') }" +
                "}"

        logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        logger.isErrorEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }
    }

    def "main should evaluate a script line if -e is given"() {
        when:
        Main.main '-d', '-e', script

        then:
        1 * logger.info('some message')
        1 * logger.error('error')
    }

    def "main should read script from standard input if no arg is given"() {
        given:
        def stdin = System.in
        System.in = new ByteArrayInputStream(script.bytes)

        when:
        Main.main '-d'

        then:
        1 * logger.info('some message')
        1 * logger.error('error')

        cleanup:
        System.in = stdin
    }

    def "main should read script from standard input if - is given"() {
        given:
        def stdin = System.in
        System.in = new ByteArrayInputStream(script.bytes)

        when:
        Main.main '-d', '-'

        then:
        1 * logger.info('some message')
        1 * logger.error('error')

        cleanup:
        System.in = stdin
    }

    def "main should read script from a file is path is given"() {
        given:
        def scriptFile = temporaryFolder.newFile()
        scriptFile << script

        when:
        Main.main '-d', scriptFile.path

        then:
        1 * logger.info('some message')
        1 * logger.error('error')
    }

}
