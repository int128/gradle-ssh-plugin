package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.common.keyprovider.FileKeyPairProvider

class HostKeyFixture {

    static enum KeyType {
        dsa,
        rsa,
        ecdsa
    }

    static publicKey(KeyType keyType = KeyType.dsa) {
        HostKeyFixture.getResourceAsStream("/hostkey_${keyType}.pub").text
    }

    static keyPairProvider(KeyType keyType = KeyType.dsa) {
        new FileKeyPairProvider(HostKeyFixture.getResource("/hostkey_$keyType").file)
    }

}
