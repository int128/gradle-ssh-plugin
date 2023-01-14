package org.hidetake.groovy.ssh.connection

import org.hidetake.groovy.ssh.core.Remote

import com.jcraft.jsch.AgentIdentityRepository
import com.jcraft.jsch.IdentityRepository
import com.jcraft.jsch.JSch
import com.jcraft.jsch.SSHAgentConnector
import com.jcraft.jsch.Session

import groovy.util.logging.Slf4j

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
			// Use agent authentication using https://github.com/mwiede/jsch/issues/65#issuecomment-913051572
			IdentityRepository irepo = new AgentIdentityRepository(new SSHAgentConnector())
            jsch.identityRepository = irepo
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
}