package org.hidetake.gradle.ssh.internal.task

import com.jcraft.jsch.Channel
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.task.Executor
import org.hidetake.gradle.ssh.internal.operation.DefaultHandler
import org.hidetake.gradle.ssh.internal.operation.OperationProxy
import org.hidetake.gradle.ssh.internal.session.ChannelManager
import org.hidetake.gradle.ssh.internal.session.SessionManager

/**
 * A default implementation of executor.
 *
 * @author hidetake.org
 */
@Singleton
@Slf4j
class WetRun implements Executor {
    @Override
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs) {
        def sessionManager = new SessionManager(sshSettings)
        def channelManager = new ChannelManager()
        try {
            sessionSpecs.collect { sessionSpec ->
                def session = sessionManager.create(sessionSpec.remote)
                def operation = sessionSpec.operationClosure
                def handler = new DefaultHandler(sshSettings, sessionSpec.remote, session, channelManager)
                operation.delegate = new OperationProxy(handler)
                operation.resolveStrategy = Closure.DELEGATE_FIRST
                operation
            }.each {
                it.call()
            }

            channelManager.waitForPending { Channel channel ->
                log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
            }
            channelManager.validateExitStatus()
        } finally {
            channelManager.disconnect()
            sessionManager.disconnect()
        }
    }
}
