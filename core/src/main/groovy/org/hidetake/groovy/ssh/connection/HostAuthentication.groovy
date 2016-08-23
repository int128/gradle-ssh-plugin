package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

@Slf4j
trait HostAuthentication {

    void validateHostAuthentication(HostAuthenticationSettings settings, Remote remote) {
        switch (settings.knownHosts) {
            case File:
                def file = settings.knownHosts as File
                if (!file.exists()) {
                    throw new FileNotFoundException("knownHosts file not found: $file")
                }
                break
            case Collection:
                break
            case AddHostKey:
                break
            case AllowAnyHosts:
                log.warn('Host key checking is off. It may be vulnerable to man-in-the-middle attacks.')
                break
            default:
                throw new IllegalArgumentException("knownHosts must be allowAnyHosts, addHostKey, a File or collection of files: $settings.knownHosts")
        }
    }

    void configureHostAuthentication(JSch jsch, Session session, Remote remote, HostAuthenticationSettings settings) {
        def helper = new Helper(session, remote)
        switch (settings.knownHosts) {
            case File:
                def file = settings.knownHosts as File
                helper.configureHostAuthentication('yes', [file])
                break
            case Collection:
                def files = settings.knownHosts as Collection<File>
                helper.configureHostAuthentication('yes', files)
                break
            case AddHostKey:
                def file = (settings.knownHosts as AddHostKey).knownHostsFile
                if (file.createNewFile()) {
                    log.info("Created known-hosts file: $file")
                }
                helper.configureHostAuthentication('ask', [file])
                break
            case AllowAnyHosts:
                helper.configureHostAuthentication('no')
                break
        }
    }

    void addHostKeyToKnownHostsFile(AddHostKey addHostKey, Session session, Remote remote) {
        def helper = new Helper(session, remote)
        helper.addHostKeyToKnownHostsFile(addHostKey.knownHostsFile)
    }

    @Slf4j
    private static class Helper {
        final Session session
        final Remote remote

        def Helper(Session session1, Remote remote1) {
            session = session1
            remote = remote1
        }

        boolean isViaGateway() {
            [session.host, session.port] != [remote.host, remote.port]
        }

        void configureHostAuthentication(String strictHostKeyChecking, Collection<File> knownHostsFiles = []) {
            session.setConfig('StrictHostKeyChecking', strictHostKeyChecking)
            knownHostsFiles.each { file ->
                log.debug("Using known-hosts file for $remote: $file")
                configureKnownHostsFile(file)
            }
            configureKeyExchangeAlgorithm()
        }

        void addHostKeyToKnownHostsFile(File knownHostsFile) {
            def hostKeys = HostKeyRepository.create(session).findAll()
            def repository = HostKeyRepository.create(knownHostsFile)
            if (viaGateway) {
                repository.addAll(
                    hostKeys.collect { hostKey ->
                        log.debug("Adding translated host key for remote: $session.host:$session.port -> $remote.host:$remote.port")
                        HostKeyRepository.translateHostPort(hostKey, remote.host, remote.port)
                    })
            } else {
                repository.addAll(hostKeys)
            }
        }

        private void configureKnownHostsFile(File knownHostsFile) {
            def hostKeys = HostKeyRepository.create(knownHostsFile).findAll()
            def repository = HostKeyRepository.create(session)
            if (viaGateway) {
                repository.addAll(
                    hostKeys.collect { hostKey ->
                        if (HostKeyRepository.compare(hostKey, remote.host, remote.port)) {
                            log.debug("Using translated host key for gateway: $remote.host:$remote.port -> $session.host:$session.port")
                            HostKeyRepository.translateHostPort(hostKey, session.host, session.port)
                        } else {
                            hostKey
                        }
                    })
            } else {
                repository.addAll(hostKeys)
            }
        }

        private void configureKeyExchangeAlgorithm() {
            def keys = HostKeyRepository.create(session).findAll(session.host, session.port)
            def keyTypes = keys*.type.unique().join(',')
            if (keyTypes) {
                session.setConfig('server_host_key', keyTypes)
                log.debug("Using key exhange algorithm for $remote: $keyTypes")
            }
        }
    }

}