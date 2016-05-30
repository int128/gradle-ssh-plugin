package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

@Slf4j
trait HostAuthentication {

    void validateHostAuthentication(HostAuthenticationSettings settings, Remote remote) {
        assert settings.knownHosts != null, "knownHosts must not be null (remote ${remote.name})"
    }

    void configureHostAuthentication(JSch jsch, Session session, Remote remote, HostAuthenticationSettings settings) {
        if (settings.knownHosts == ConnectionSettings.Constants.allowAnyHosts) {
            session.setConfig('StrictHostKeyChecking', 'no')
            log.warn("Strict host key checking is off. It may be vulnerable to man-in-the-middle attacks.")
        } else {
            session.setConfig('StrictHostKeyChecking', 'yes')
            log.debug("Using known-hosts file for $remote.name: $settings.knownHosts.path")

            jsch.setKnownHosts(settings.knownHosts.path)
            def hostKeys = new HostKeys(session.hostKeyRepository.hostKey.toList())
            def keyTypes = hostKeys.keyTypes(session.host, session.port).join(',')
            if (keyTypes) {
                session.setConfig('server_host_key', keyTypes)
                log.debug("Using key exhange algorithm for $remote.name: $keyTypes")
            }
        }
    }

}