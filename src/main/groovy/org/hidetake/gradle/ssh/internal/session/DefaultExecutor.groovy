package org.hidetake.gradle.ssh.internal.session

import com.jcraft.jsch.Channel
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Executor
import org.hidetake.gradle.ssh.api.session.SessionHandlerFactory
import org.hidetake.gradle.ssh.registry.Registry

/**
 * A default implementation of executor.
 *
 * @author hidetake.org
 */
@Singleton
@Slf4j
class DefaultExecutor implements Executor {
    @Override
    void execute(SshSettings sshSettings, List<SessionSpec> sessionSpecs) {
        def factory = Registry.instance[SessionHandlerFactory]

        def sessionManager = new SessionManager(sshSettings)
        def channelManager = new ChannelManager()
        try {
            sessionSpecs.collect { sessionSpec ->
                def sessionHandler = factory.create(sessionSpec.remote, sessionManager, channelManager, sshSettings)
                sessionSpec.operationClosure.delegate = sessionHandler
                sessionSpec.operationClosure.resolveStrategy = Closure.DELEGATE_FIRST
                sessionSpec.operationClosure
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
