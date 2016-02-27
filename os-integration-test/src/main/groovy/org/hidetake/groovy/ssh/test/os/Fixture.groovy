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

    static privateKey() {
        new File(dotSsh, 'id_rsa')
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

    static privateKeyWithPassphrase() {
        new File(dotSsh, 'id_rsa_passphrase')
    }

    static passphraseOfPrivateKey() {
        'pass1234'
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

}
