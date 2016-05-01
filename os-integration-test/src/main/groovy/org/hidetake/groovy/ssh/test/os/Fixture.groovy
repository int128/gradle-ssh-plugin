package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.core.Service

class Fixture {

    static randomInt(int max = 10000) {
        (Math.random() * max) as int
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

    static createRemote(Service service, String name) {
        service.remotes.create(name) {
            host        = requireProperty('ssh.host')
            user        = requireProperty('ssh.user')
            identity    = file optionalProperty('ssh.key.path')
            passphrase  = optionalProperty('ssh.key.passphrase')
            knownHosts  = file optionalProperty('ssh.knownHosts')
            agent       = flag optionalProperty('ssh.agent')
        }
    }

    private static String requireProperty(String key) {
        assert System.getProperty(key)
        System.getProperty(key)
    }

    private static String optionalProperty(String key) {
        System.getProperty(key)
    }

    private static File file(String path) {
        path ? new File(path) : null
    }

    private static Boolean flag(String value) {
        value ? true : null
    }

}
