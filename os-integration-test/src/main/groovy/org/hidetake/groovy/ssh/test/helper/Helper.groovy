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
        new File("${System.getProperty('user.home')}/.ssh/id_rsa")
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

}
