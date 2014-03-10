package org.hidetake.gradle.ssh.internal.session

import com.jcraft.jsch.Channel
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.operation.OperationsFactory
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
        def sessionHandlerFactory = Registry.instance[SessionHandlerFactory]
        def operationsFactory = Registry.instance[OperationsFactory]

        def sessionManager = new SessionManager(sshSettings)
        def channelManager = new ChannelManager()
        try {
            sessionSpecs.collect { sessionSpec ->
                if (sshSettings.dryRun) {
                    sessionSpec.operationClosure.delegate = sessionHandlerFactory.create()
                } else {
                    def session = sessionManager.create(sessionSpec.remote)
                    sessionSpec.operationClosure.delegate = sessionHandlerFactory.create(
                        operationsFactory.create(
                            sessionSpec.remote, session, channelManager, sshSettings))
                }
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
