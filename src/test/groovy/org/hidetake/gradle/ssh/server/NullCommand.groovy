package org.hidetake.gradle.ssh.server

import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

/**
 * Null implementation of {@link Command}.
 *
 * @author hidetake.org
 *
 */
class NullCommand implements Command {
    final int exitValue

    NullCommand(int exitValue) {
        this.exitValue = exitValue
    }

    @Override
    void setInputStream(InputStream inputStream) {
    }

    @Override
    void setOutputStream(OutputStream outputStream) {
    }

    @Override
    void setErrorStream(OutputStream errorStream) {
    }

    @Override
    void setExitCallback(ExitCallback callback) {
        callback.onExit(exitValue)
    }

    @Override
    void start(Environment env) throws IOException {
    }

    @Override
    void destroy() {
    }
}
