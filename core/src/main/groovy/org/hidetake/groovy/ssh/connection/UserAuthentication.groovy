package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

@Slf4j
trait UserAuthentication {

    void validateUserAuthentication(UserAuthenticationSettings settings, Remote remote) {
        assert settings.user, "user must be given ($remote)"
        assert settings.identity instanceof File || settings.identity instanceof String || settings.identity == null,
                "identity must be a File, String or null ($remote)"
    }

    void configureUserAuthentication(JSch jsch, Session session, Remote remote, UserAuthenticationSettings settings) {
        session.setConfig('PreferredAuthentications', settings.authentications.join(','))

        if (settings.password) {
            session.password = settings.password
            log.debug("Using password authentication for $remote")
        }

        if (settings.agent) {
            jsch.identityRepository = RemoteIdentityRepositoryLocator.get()
            log.debug("Using SSH agent authentication for $remote")
        } else {
            jsch.identityRepository = null    /* null means the default repository */
            jsch.removeAllIdentity()
            if (settings.identity) {
                final identity = settings.identity
                if (identity instanceof File) {
                    jsch.addIdentity(identity.path, settings.passphrase as String)
                    log.debug("Using public key authentication for $remote: $identity.path")
                } else if (identity instanceof String) {
                    jsch.addIdentity("identity-${identity.hashCode()}", identity.bytes, null, settings.passphrase?.bytes)
                    log.debug("Using public key authentication for $remote")
                }
            }
        }
    }

    private static class RemoteIdentityRepositoryLocator {
        private static instance = null

        static get() {
            if (instance) {
                instance
            } else {
                instance = new RemoteIdentityRepository(ConnectorFactory.default.createConnector())
            }
        }
    }

}
