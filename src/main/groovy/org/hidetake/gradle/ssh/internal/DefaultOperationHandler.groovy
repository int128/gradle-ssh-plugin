package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import groovy.transform.TupleConstructor
import org.codehaus.groovy.tools.Utilities
import org.hidetake.gradle.ssh.api.*

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

    final listeners = [] as List<OperationEventListener>

    @Override
    Remote getRemote() {
        sessionSpec.remote
    }

    @Override
    String execute(String command) {
        execute([:], command)
    }

    @Override
    String execute(Map<String, Object> options, String command) {
        listeners*.beginOperation('execute', options, command)

        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        options.each { k, v -> channel[k] = v }

        def outputLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.outputLogLevel, sshSpec.encoding)
        def errorLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.errorLogLevel, sshSpec.encoding)
        channel.outputStream = outputLogger
        channel.errStream = errorLogger

        try {
            channel.connect()
            listeners*.managedChannelConnected(channel, sessionSpec)
            while (!channel.closed) {
                sleep(100)
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
    String executeSudo(Map<String, Object> options, String command) {
        listeners*.beginOperation('executeSudo', command, options)

        def channel = session.openChannel('exec') as ChannelExec
        channel.command = "sudo -S -p '' $command"
        options.each { k, v -> channel[k] = v }

        def outputLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.outputLogLevel, sshSpec.encoding)
        def errorLogger = new LoggingOutputStream(sshSpec.logger, sshSpec.errorLogLevel, sshSpec.encoding)
        channel.outputStream = outputLogger
        channel.errStream = errorLogger

        // filter password and check authentication failure.
        boolean authenticationFailed = false
        int lineNumber = 0
        outputLogger.filter = { String line ->
            // usually password or messages appear within a few lines.
            if (++lineNumber < 5) {
                if (line.contains('try again')) {
                    // this closure runs in I/O thread, so needs to notify failure to main thread.
                    authenticationFailed = true
                }
                !line.contains(sessionSpec.remote.password)
            } else {
                true
            }
        }

        try {
            channel.connect()
            listeners*.managedChannelConnected(channel, sessionSpec)
            channel.outputStream.withWriter(sshSpec.encoding) {
                it << sessionSpec.remote.password << '\n'
            }
            while (!channel.closed) {
                if (authenticationFailed) {
                    throw new RuntimeException('Unable to execute sudo command. Wrong username/password')
                }
                sleep(100)
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
    void executeBackground(Map<String, Object> options, String command) {
        listeners*.beginOperation('executeBackground', command)

        def channel = session.openChannel('exec') as ChannelExec
        channel.command = command
        channel.outputStream = new LoggingOutputStream(sshSpec.logger, sshSpec.outputLogLevel, sshSpec.encoding)
        channel.errStream = new LoggingOutputStream(sshSpec.logger, sshSpec.errorLogLevel, sshSpec.encoding)
        options.each { k, v -> channel[k] = v }

        channel.connect()
        listeners*.unmanagedChannelConnected(channel, sessionSpec)
    }

    @Override
    void get(String remote, String local) {
        get([:], remote, local)
    }

    @Override
    void get(Map<String, Object> options, String remote, String local) {
        listeners*.beginOperation('get', remote, local)
        def channel = session.openChannel('sftp') as ChannelSftp
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
    void put(Map<String, Object> options, String local, String remote) {
        listeners*.beginOperation('put', remote, local)
        def channel = session.openChannel('sftp') as ChannelSftp
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
