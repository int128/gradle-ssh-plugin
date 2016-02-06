package org.hidetake.groovy.ssh.test.helper

class Helper {

    static randomInt(int max = 10000) {
        (Math.random() * max) as int
    }

    static hostName() {
        System.getenv('INTEGRATION_TEST_SSH_HOST') ?: 'localhost'
    }

    static userName() {
        System.getenv('INTEGRATION_TEST_SSH_USER') ?: System.getProperty('user.name')
    }

    static privateKey() {
        final dotSsh = new File("${System.getProperty('user.home')}/.ssh")
        new File(dotSsh, System.getenv('INTEGRATION_TEST_SSH_KEY_NAME') ?: 'id_rsa')
    }

    static privateKeyWithPassphrase() {
        final dotSsh = new File("${System.getProperty('user.home')}/.ssh")
        new File(dotSsh, System.getenv('INTEGRATION_TEST_SSH_PASSPHRASE_KEY_NAME') ?: 'id_rsa_passphrase')
    }

    static passphraseOfPrivateKey() {
        'pass1234'
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

}
