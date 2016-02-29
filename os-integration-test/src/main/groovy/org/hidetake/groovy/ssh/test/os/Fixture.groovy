package org.hidetake.groovy.ssh.test.os

class Fixture {

    static dotSsh = new File("${System.getProperty('user.home')}/.ssh")

    static randomInt(int max = 10000) {
        (Math.random() * max) as int
    }

    static hostName() {
        'localhost'
    }

    static userName() {
        System.getProperty('user.name')
    }

    static privateKeyRSA() {
        new File(dotSsh, 'id_rsa')
    }

    static privateKeyRSAWithPassphrase() {
        new File(dotSsh, 'id_rsa_passphrase')
    }

    static privateKeyECDSA() {
        new File(dotSsh, 'id_ecdsa')
    }

    static hostNameForPrivilegeAccess() {
        System.getenv('INTEGRATION_TEST_SSH_HOST')
    }

    static userNameForPrivilegeAccess() {
        System.getenv('INTEGRATION_TEST_SSH_USER')
    }

    static privateKeyForPrivilegeAccess() {
        new File(System.getenv('INTEGRATION_TEST_SSH_KEY_PATH'))
    }

    static passphraseOfPrivateKey() {
        'pass1234'
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

}
