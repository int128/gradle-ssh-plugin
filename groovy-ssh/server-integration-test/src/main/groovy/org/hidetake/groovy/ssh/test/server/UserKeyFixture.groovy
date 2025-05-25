package org.hidetake.groovy.ssh.test.server

class UserKeyFixture {

    static enum KeyType {
        ecdsa,
        ecdsa_pass
    }

    static privateKey(KeyType keyType = KeyType.ecdsa) {
        new File(UserKeyFixture.getResource("/id_$keyType").file)
    }

    static publicKey(KeyType keyType = KeyType.ecdsa) {
        new File(UserKeyFixture.getResource("/id_${keyType}.pub").file)
    }

}
