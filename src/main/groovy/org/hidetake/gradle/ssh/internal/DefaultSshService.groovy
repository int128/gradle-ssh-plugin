package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import org.hidetake.gradle.ssh.internal.session.ChannelManager
import org.hidetake.gradle.ssh.internal.session.GatewaySessionTransformation
import org.hidetake.gradle.ssh.internal.session.SessionManager

/**
 * Default implementation of {@link SshService}.
 *
 * @author hidetake.org
 *
 */
@Singleton
@Slf4j
class DefaultSshService implements SshService {
    @Override
    void execute(SshSpec sshSpec) {
        assert sshSpec.dryRun == Boolean.FALSE, 'dryRun should be false'

        def sessionManager = new SessionManager(sshSpec)
        def channelManager = new ChannelManager()
        try {
            def sessionSpecs = GatewaySessionTransformation.transform(sshSpec.sessionSpecs)
            sessionSpecs.each { sessionSpec ->
                def session = sessionManager.create(sessionSpec.remote)

                def handler = new DefaultOperationHandler(sshSpec, sessionSpec, session, channelManager)
                handler.with(sessionSpec.operationClosure)
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
