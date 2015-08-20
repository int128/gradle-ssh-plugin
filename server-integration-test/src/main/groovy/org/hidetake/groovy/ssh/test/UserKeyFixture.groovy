package org.hidetake.groovy.ssh.test

class UserKeyFixture {

    static enum KeyType {
        rsa,
        rsa_pass,
        ecdsa
    }

    static privateKey(KeyType keyType = KeyType.rsa) {
        new File(UserKeyFixture.getResource("/id_$keyType").file)
    }

    static publicKey(KeyType keyType = KeyType.rsa) {
        new File(UserKeyFixture.getResource("/id_${keyType}.pub").file)
    }

}
