package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

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

    static command(int status, @DelegatesTo(CommandContext) Closure interaction = {}) {
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
                log.debug("[ssh-server-mock] Started interaction thread")
                try {
                    callWithDelegate(interaction, context)
                    context.exitCallback.onExit(status)
                } catch (Throwable t) {
                    log.error("[ssh-server-mock] Error occurred on interaction thread", t)
                    context.exitCallback.onExit(-1, t.message)
                }
                log.debug("[ssh-server-mock] Terminated interaction thread")
            }
        },
        destroy: { ->
        }] as Command
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
