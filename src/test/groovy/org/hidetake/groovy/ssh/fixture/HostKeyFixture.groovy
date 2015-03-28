package org.hidetake.groovy.ssh.fixture

class HostKeyFixture {

    static enum KeyType {
        dsa,
        rsa
    }

    static privateKey(KeyType keyType = KeyType.dsa) {
        new File(HostKeyFixture.getResource("/hostkey_$keyType").file)
    }

    static publicKey(KeyType keyType = KeyType.dsa) {
        new File(HostKeyFixture.getResource("/hostkey_${keyType}.pub").file)
    }

}
