package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.common.keyprovider.FileKeyPairProvider

class HostKeyFixture {

    static publicKey(String keyType) {
        HostKeyFixture.getResourceAsStream("/hostkey_${keyType}.pub").text
    }

    static publicKeys(List<String> keyTypes) {
        keyTypes.collect { keyType ->  publicKey(keyType) }
    }

    static keyPairProvider(String... keyTypes) {
        keyPairProvider(keyTypes.toList())
    }

    static keyPairProvider(List<String> keyTypes) {
        new FileKeyPairProvider(keyTypes.collect { keyType ->
            HostKeyFixture.getResource("/hostkey_$keyType").file
        } as String[])
    }

}
