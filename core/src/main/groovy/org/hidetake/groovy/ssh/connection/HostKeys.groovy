package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.HostKey

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * A list of host keys in a known hosts.
 *
 * @author Hidetake Iwata
 */
class HostKeys {

    private final List<HostKey> items

    def HostKeys(List<HostKey> items1) {
        items = items1
    }

    List<HostKey> find(String host, int port) {
        items.findAll { item ->
            item.host == host ||
            item.host == "[$host]:$port" as String ||
            compareHashedItem(item.host, host) ||
            compareHashedItem(item.host, "[$host]:$port")
        }
    }

    List<String> keyTypes(String host, int port) {
        find(host, port)*.type.unique()
    }

    static boolean compareHashedItem(String knownHostsItem, String host) {
        def matcher = (~/^\|1\|(.+?)\|(.+?)$/).matcher(knownHostsItem)
        if (matcher) {
            def salt = matcher.group(1)
            def hash = matcher.group(2)
            hmacSha1(salt.decodeBase64(), host.bytes) == hash.decodeBase64()
        } else {
            false
        }
    }

    static byte[] hmacSha1(byte[] salt, byte[] data) {
        def key = new SecretKeySpec(salt, 'HmacSHA1')
        def mac = Mac.getInstance(key.algorithm)
        mac.init(key)
        mac.doFinal(data)
    }

}
