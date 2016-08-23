package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

trait HostAuthentication {

    void validateHostAuthentication(HostAuthenticationSettings settings, Remote remote) {
        switch (settings.knownHosts) {
            case AllowAnyHosts:
                break
            case AddHostKey:
                break
            case File:
                def file = settings.knownHosts as File
                if (!file.exists()) {
                    throw new FileNotFoundException("knownHosts file not found: $file")
                }
                break
            case Collection:
                break
            default:
                throw new IllegalArgumentException("knownHosts must be allowAnyHosts, addHostKey, a File or collection of files: $settings.knownHosts")
        }
    }

    void configureHostAuthentication(JSch jsch, Session session, Remote remote, HostAuthenticationSettings settings) {
        def helper = new Helper(jsch, session, remote)
        //noinspection GroovyAssignabilityCheck
        helper.configureHostAuthentication(settings.knownHosts)
    }

    void configureToAddNewHostKey(HostAuthenticationSettings settings) {
        assert settings.knownHosts instanceof AddHostKey
        settings.knownHosts = new AddNewHostKey(settings.knownHosts as AddHostKey)
    }

    private static class AddNewHostKey {
        final AddHostKey addHostKey

        def AddNewHostKey(AddHostKey addHostKey1) {
            addHostKey = addHostKey1
        }

        @Override
        String toString() {
            addHostKey.toString()
        }
    }

    @Slf4j
    private static class Helper {
        final JSch jsch
        final Session session
        final Remote remote

        def Helper(JSch jsch1, Session session1, Remote remote1) {
            jsch = jsch1
            session = session1
            remote = remote1
        }

        void configureHostAuthentication(AllowAnyHosts allowAnyHosts) {
            log.warn('Host key checking is off. It may be vulnerable to man-in-the-middle attacks.')
            session.setConfig('StrictHostKeyChecking', 'no')
        }

        void configureHostAuthentication(AddHostKey addHostKey) {
            def file = addHostKey.knownHostsFile
            if (file.createNewFile()) {
                log.info("Created known-hosts file: $file")
            }
            log.debug("Using known-hosts file for $remote: $file")
            jsch.setKnownHosts(file.path)
            session.setConfig('StrictHostKeyChecking', 'ask')
            configureHostKeyTypes()
            configureForGateway()
        }

        void configureHostAuthentication(AddNewHostKey addNewHostKey) {
            def file = addNewHostKey.addHostKey.knownHostsFile
            log.info("Adding host key of $remote to known-hosts file")
            jsch.setKnownHosts(file.path)
            session.setConfig('StrictHostKeyChecking', 'no')
            configureHostKeyTypes()
            configureForGateway()
        }

        void configureHostAuthentication(File file) {
            log.debug("Using known-hosts file for $remote: $file")
            jsch.setKnownHosts(file.path)
            session.setConfig('StrictHostKeyChecking', 'yes')
            configureHostKeyTypes()
            configureForGateway()
        }

        void configureHostAuthentication(Collection<File> files) {
            log.debug("Using known-hosts files for $remote: $files")
            def hostKeys = HostKeys.fromKnownHosts(files)
            hostKeys.each { hostKey -> session.hostKeyRepository.add(hostKey, null) }
            session.setConfig('StrictHostKeyChecking', 'yes')
            configureHostKeyTypes()
            configureForGateway()
        }

        private void configureHostKeyTypes() {
            def keyTypes = HostKeys.fromSession(session).keyTypes(session.host, session.port).join(',')
            if (keyTypes) {
                session.setConfig('server_host_key', keyTypes)
                log.debug("Using key exhange algorithm for $remote: $keyTypes")
            }
        }

        private void configureForGateway() {
            if ([session.host, session.port] != [remote.host, remote.port]) {
                HostKeys.fromSession(session).find(remote.host, remote.port).each { hostKey ->
                    def hostKeyForGateway = new HostKey("[$session.host]:$session.port", hostKey.@type, hostKey.@key, hostKey.comment)
                    session.hostKeyRepository.add(hostKeyForGateway, null)
                    log.debug("Duplicated host key for gateway: $remote.host:$remote.port -> $session.host:$session.port")
                }
            }
        }
    }

}