package org.hidetake.groovy.ssh.test.helper

class Utilities {

    static enum KeyType {
        rsa,
        ecdsa
    }

    static randomInt(int max = 10000) {
        (Math.random() * max) as int
    }

    static userName() {
        System.getProperty('user.name')
    }

    static privateKey(KeyType keyType = KeyType.rsa) {
        new File("${System.getProperty('user.home')}/.ssh/id_${keyType}")
    }

}
