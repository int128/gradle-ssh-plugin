package org.hidetake.gradle.ssh.test

import org.apache.sshd.SshServer
import org.apache.sshd.common.keyprovider.FileKeyPairProvider
import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

/**
 * A helper class for server-based integration tests.
 *
 * @author hidetake.org
 *
 */
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
            interaction.call(context)
        },
        destroy: { ->
        }] as Command
    }

    static commandWithExit(int status) {
        command { CommandContext c ->
            c.exitCallback.onExit(status)
        }
    }

    static SshServer setUpLocalhostServer() {
        SshServer.setUpDefaultServer().with {
            host = 'localhost'
            port = pickUpFreePort()
            keyPairProvider = new FileKeyPairProvider(SshServerMock.getResource('/hostkey').file)
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
