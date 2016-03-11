package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

/**
 * A helper class for server-based integration tests.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SshServerMock {

    static class CommandContext {
        InputStream inputStream
        OutputStream outputStream
        OutputStream errorStream
        ExitCallback exitCallback
        Environment environment
    }

    static command(Closure interaction) {
        def context = new CommandContext()
        [setInputStream: { InputStream inputStream ->
            context.inputStream = inputStream
        },
        setOutputStream: { OutputStream outputStream ->
            context.outputStream = outputStream
        },
        setErrorStream: { OutputStream errorStream ->
            context.errorStream = errorStream
        },
        setExitCallback: { ExitCallback callback ->
            context.exitCallback = callback
        },
        start: { Environment env ->
            context.environment = env
            Thread.start {
                def threadName = Thread.currentThread().name
                log.debug("Started interaction thread $threadName")
                try {
                    interaction.call(context)
                } catch (Throwable t) {
                    log.error("Error occurred on interaction thread $threadName", t)
                    context.exitCallback.onExit(-1, t.message)
                }
                log.debug("Terminated interaction thread $threadName")
            }
        },
        destroy: { ->
        }] as Command
    }

    static commandWithExit(int status, String outputMessage = null, String errorMessage = null) {
        command { CommandContext c ->
            if (outputMessage) {
                c.outputStream.withWriter('UTF-8') { it << outputMessage }
            }
            if (errorMessage) {
                c.errorStream.withWriter('UTF-8') { it << errorMessage }
            }
            c.exitCallback.onExit(status)
        }
    }

    static SshServer setUpLocalhostServer(provider = HostKeyFixture.keyPairProvider()) {
        SshServer.setUpDefaultServer().with {
            host = 'localhost'
            port = pickUpFreePort()
            keyPairProvider = provider
            it
        }
    }

    static int pickUpFreePort() {
        def socket = new ServerSocket(0)
        def port = socket.localPort
        socket.close()
        port
    }

}
