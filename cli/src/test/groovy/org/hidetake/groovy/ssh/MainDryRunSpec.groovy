package org.hidetake.groovy.ssh

import com.jcraft.jsch.JSchException
import org.hidetake.groovy.ssh.test.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class MainDryRunSpec extends Specification {

    @Shared
    String script

    ByteArrayOutputStream stdoutBuffer

    PrintStream stdout

    @Rule
    TemporaryFolder temporaryFolder

    def setupSpec() {
        int port = SshServerMock.pickUpFreePort()
        script = "ssh.run {" +
                "session(host: 'localhost'," +
                " port: $port," +
                " user: 'someuser'," +
                " password: 'somepassword')" +
                "{ execute('somecommand') { println 'Q6zLyqR1MKANtYJ4' } }" +
                "}"
    }

    def setup() {
        stdout = System.out
        stdoutBuffer = new ByteArrayOutputStream()
        System.out = new PrintStream(stdoutBuffer)
    }

    def cleanup() {
        System.out = stdout
    }

    def "script should fail due to connection refused"() {
        when:
        Main.main '-e', script

        then:
        JSchException e = thrown()
        e.cause instanceof ConnectException
        e.cause.message.startsWith 'Connection refused'
    }

    @Unroll
    def "flag #flag should enable dry run"() {
        when:
        Main.main flag, '-e', script

        then:
        stdoutBuffer.toString('UTF-8').contains('Q6zLyqR1MKANtYJ4')

        where:
        flag << ['--dry-run', '-n']
    }

}
