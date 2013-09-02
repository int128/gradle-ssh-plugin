package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import groovy.transform.TupleConstructor
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.OperationHandler
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSpec

/**
 * Default implementation of {@link OperationHandler}.
 *
 * @author hidetake.org
 *
 */
@TupleConstructor
class DefaultOperationHandler implements OperationHandler {
    final SshSpec sshSpec
    final SessionSpec sessionSpec
    final Session session

    /**
     * Event listeners.
     */
    final List<OperationEventListener> listeners = []

    @Override
    Remote getRemote() {
        sessionSpec.remote
    }

    @Override
    String execute(String command) {
        execute([:], command)
    }

    @Override
    String execute(Map options, String command) {
        listeners*.beginOperation('execute', options, command)

        ChannelExec channel = session.openChannel('exec')
        channel.command = command
        options.each { k, v -> channel[k] = v }

        def outputLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.outputLogLevel)
        def errorLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.errorLogLevel)
        channel.outputStream = outputLogger
        channel.errStream = errorLogger

        try {
            channel.connect()
            listeners*.managedChannelConnected(channel, sessionSpec)
            while (!channel.closed) {
                Thread.sleep(500)
            }
            listeners*.managedChannelClosed(channel, sessionSpec)
        } finally {
            channel.disconnect()
            outputLogger.close()
            errorLogger.close()
        }

        outputLogger.lines.join(Utilities.eol())
    }

    @Override
    String executeSudo(String command) {
        executeSudo([:], command)
    }

    @Override
    String executeSudo(Map options, String command) {
        listeners*.beginOperation('executeSudo', command, options)

        ChannelExec channel = session.openChannel('exec') as ChannelExec
        channel.command = "sudo -S -p '' $command"
        options.each { k, v -> channel[k] = v }

        def outputLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.outputLogLevel)
        def errorLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.errorLogLevel)
        channel.outputStream = outputLogger
        channel.errStream = errorLogger

        // TODO: check "try again"
        outputLogger.filter = { String line -> !line.contains(sessionSpec.remote.password) }

        try {
            channel.connect()
            listeners*.managedChannelConnected(channel, sessionSpec)
            channel.outputStream.withWriter {
                it.write(sessionSpec.remote.password + '\n')
            }
            while (!channel.closed) {
                Thread.sleep(500)
            }
            listeners*.managedChannelClosed(channel, sessionSpec)
        } finally {
            channel.disconnect()
            outputLogger.close()
            errorLogger.close()
        }

        outputLogger.lines.join(Utilities.eol())
    }

    @Override
    void executeBackground(String command) {
        executeBackground([:], command)
    }

    @Override
    void executeBackground(Map options, String command) {
        listeners*.beginOperation('executeBackground', command)

        ChannelExec channel = session.openChannel('exec')
        channel.command = command
        channel.outputStream = new LoggingOutputStream(sshSpec.logger, sshSpec.outputLogLevel)
        channel.errStream = new LoggingOutputStream(sshSpec.logger, sshSpec.errorLogLevel)
        options.each { k, v -> channel[k] = v }

        channel.connect()
        listeners*.unmanagedChannelConnected(channel, sessionSpec)
    }

    @Override
    void get(String remote, String local) {
        get([:], remote, local)
    }

    @Override
    void get(Map options, String remote, String local) {
        listeners*.beginOperation('get', remote, local)
        ChannelSftp channel = session.openChannel('sftp')
        options.each { k, v -> channel[k] = v }
        try {
            channel.connect()
            listeners*.managedChannelConnected(channel, sessionSpec)
            channel.get(remote, local)
            listeners*.managedChannelClosed(channel, sessionSpec)
        } finally {
            channel.disconnect()
        }
    }

    @Override
    void put(String local, String remote) {
        put([:], local, remote)
    }

    @Override
    void put(Map options, String local, String remote) {
        listeners*.beginOperation('put', remote, local)
        ChannelSftp channel = session.openChannel('sftp')
        options.each { k, v -> channel[k] = v }
        try {
            channel.connect()
            listeners*.managedChannelConnected(channel, sessionSpec)
            channel.put(local, remote, ChannelSftp.OVERWRITE)
            listeners*.managedChannelClosed(channel, sessionSpec)
        } finally {
            channel.disconnect()
        }
    }
}
