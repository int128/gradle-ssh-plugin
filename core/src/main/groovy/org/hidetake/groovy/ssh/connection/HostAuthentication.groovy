package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

@Slf4j
trait HostAuthentication {

    void validateHostAuthentication(HostAuthenticationSettings settings, Remote remote) {
        assert settings.knownHosts != null, "knownHosts must not be null (remote ${remote.name})"
        assert settings.knownHosts instanceof AllowAnyHosts || settings.knownHosts instanceof File || settings.knownHosts instanceof Collection
    }

    void configureHostAuthentication(JSch jsch, Session session, Remote remote, HostAuthenticationSettings settings) {
        switch (settings.knownHosts) {
            case AllowAnyHosts:
                session.setConfig('StrictHostKeyChecking', 'no')
                log.warn('Host key checking is off. It may be vulnerable to man-in-the-middle attacks.')
                break

            case File:
                def file = settings.knownHosts as File
                session.setConfig('StrictHostKeyChecking', 'yes')
                log.debug("Using known-hosts file for $remote.name: $file")

                jsch.setKnownHosts(file.path)
                def hostKeys = new HostKeys(jsch.hostKeyRepository.hostKey.toList())
                def keyTypes = hostKeys.keyTypes(session.host, session.port).join(',')
                if (keyTypes) {
                    session.setConfig('server_host_key', keyTypes)
                    log.debug("Using key exhange algorithm for $remote.name: $keyTypes")
                }
                break

            case Collection:
                def files = settings.knownHosts as Collection<File>
                session.setConfig('StrictHostKeyChecking', 'yes')
                log.debug("Using known-hosts files for $remote.name: $files")

                def hostKeys = HostKeys.fromKnownHosts(files)
                hostKeys.addTo(session.hostKeyRepository)

                def keyTypes = hostKeys.keyTypes(session.host, session.port).join(',')
                if (keyTypes) {
                    session.setConfig('server_host_key', keyTypes)
                    log.debug("Using key exhange algorithm for $remote.name: $keyTypes")
                }
                break

            default:
                throw new IllegalArgumentException("knownHosts must be AllowAnyHosts, File or List")
        }
    }

}