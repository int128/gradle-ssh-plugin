package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import groovy.util.logging.Slf4j

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * A list of host keys in a known hosts.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class HostKeys {

    @Delegate
    private final Collection<HostKey> items

    def HostKeys(Collection<HostKey> items1) {
        items = items1
    }

    Collection<HostKey> find(String host, int port) {
        items.findAll { item ->
            item.host == host ||
            item.host == "[$host]:$port" as String ||
            compareHashedItem(item.host, host) ||
            compareHashedItem(item.host, "[$host]:$port")
        }
    }

    Collection<String> keyTypes(String host, int port) {
        find(host, port)*.type.unique()
    }

    static HostKeys fromSession(Session session) {
        new HostKeys(session.hostKeyRepository.hostKey.toList())
    }

    static HostKeys fromKnownHosts(Collection<File> files) {
        def jsch = new JSch()
        def items = files.collect { file ->
            jsch.setKnownHosts(file.path)
            jsch.hostKeyRepository.hostKey
        }.flatten() as List<HostKey>
        new HostKeys(items)
    }

    private static boolean compareHashedItem(String knownHostsItem, String host) {
        def matcher = (~/^\|1\|(.+?)\|(.+?)$/).matcher(knownHostsItem)
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
