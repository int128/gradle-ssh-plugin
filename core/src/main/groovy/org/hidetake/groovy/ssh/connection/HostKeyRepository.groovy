package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.HostKey
import com.jcraft.jsch.HostKeyRepository as JSchHostKeyRepository
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * A thin wrapper of {@link com.jcraft.jsch.HostKeyRepository}.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class HostKeyRepository {

    private final JSchHostKeyRepository hostKeyRepository

    def HostKeyRepository(JSchHostKeyRepository hostKeyRepository1) {
        hostKeyRepository = hostKeyRepository1
    }

    Collection<HostKey> findAll() {
        hostKeyRepository.hostKey.toList()
    }

    Collection<HostKey> findAll(String host, int port) {
        hostKeyRepository.hostKey.findAll { hostKey -> compare(hostKey, host, port) }
    }

    void addAll(Collection<HostKey> hostKeys) {
        hostKeys.each { hostKey ->
            hostKeyRepository.add(hostKey, null)
        }
    }

    static HostKeyRepository create(Session session) {
        new HostKeyRepository(session.hostKeyRepository)
    }

    static HostKeyRepository create(File knownHostsFile) {
        def jsch = new JSch()
        jsch.setKnownHosts(knownHostsFile.path)
        new HostKeyRepository(jsch.hostKeyRepository)
    }

    static HostKey translateHostPort(HostKey hostKey, String host, int port) {
        if (port == 22) {
            new HostKey(host, hostKey.@type, hostKey.@key, hostKey.comment)
        } else {
            new HostKey("[$host]:$port", hostKey.@type, hostKey.@key, hostKey.comment)
        }
    }

    static boolean compare(HostKey hostKey, String host, int port) {
        if (port == 22) {
            hostKey.host == host || compareHashed(hostKey, host)
        } else {
            hostKey.host == "[$host]:$port" as String || compareHashed(hostKey, "[$host]:$port")
        }
    }

    private static boolean compareHashed(HostKey hostKey, String host) {
        def matcher = (~/^\|1\|(.+?)\|(.+?)$/).matcher(hostKey.host)
        if (matcher) {
            def salt = matcher.group(1)
            def hash = matcher.group(2)
            hmacSha1(salt.decodeBase64(), host.bytes) == hash.decodeBase64()
        } else {
            false
        }
    }

    private static byte[] hmacSha1(byte[] salt, byte[] data) {
        def key = new SecretKeySpec(salt, 'HmacSHA1')
        def mac = Mac.getInstance(key.algorithm)
        mac.init(key)
        mac.doFinal(data)
    }

}
