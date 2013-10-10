package org.hidetake.gradle.ssh.test

import groovy.transform.TupleConstructor
import org.apache.sshd.SshServer
import org.apache.sshd.common.keyprovider.FileKeyPairProvider
import org.apache.sshd.server.Command
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

/**
 * A helper class for server-based integration tests.
 *
 * @author hidetake.org
 *
 */
class ServerBasedTestHelper {

    static class CommandContext {
        InputStream inputStream
        OutputStream outputStream
        OutputStream errorStream
        ExitCallback exitCallback
    }

    static abstract class AbstractCommand implements Command {
        final context = new CommandContext()

        @Override
        void setInputStream(InputStream inputStream) {
            context.inputStream = inputStream
        }

        @Override
        void setOutputStream(OutputStream outputStream) {
            context.outputStream = outputStream
        }

        @Override
        void setErrorStream(OutputStream errorStream) {
            context.errorStream = errorStream
        }

        @Override
        void setExitCallback(ExitCallback callback) {
            context.exitCallback = callback
        }

        @Override
        void destroy() {
        }
    }

    @TupleConstructor
    static class CommandRecorder implements CommandFactory {
        final int exitStatus = 0
        final List<String> commands = []

        @Override
        Command createCommand(String command) {
            commands << command
            new AbstractCommand() {
                @Override
                void start(Environment environment) {
                    context.exitCallback.onExit(exitStatus)
                }
            }
        }
    }

    static SshServer setUpLocalhostServer() {
        SshServer.setUpDefaultServer().with {
            host = 'localhost'
            port = pickUpFreePort()
            keyPairProvider = new FileKeyPairProvider(ServerBasedTestHelper.getResource('/hostkey').file)
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
