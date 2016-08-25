package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.core.Service

class Fixture {

    static randomInt(int max = 10000) {
        (Math.random() * max) as int
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

    static createRemotes(Service service) {
        final buildDir = 'build'
        service.remotes {
            Default {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                identity = new File("$buildDir/.ssh/id_rsa")
                knownHosts = addHostKey(new File("$buildDir/.ssh/known_hosts"))
            }
            DefaultWithPassphrase {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                identity = new File("$buildDir/.ssh/id_rsa_passphrase")
                passphrase = 'pass1234'
                knownHosts = addHostKey(new File("$buildDir/.ssh/known_hosts"))
            }
            DefaultWithOpenSSHKnownHosts {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                identity = new File("$buildDir/.ssh/id_rsa")
                knownHosts = new File("$buildDir/.ssh/known_hosts_openssh")
            }
            DefaultWithAgent {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                knownHosts = addHostKey(new File("$buildDir/.ssh/known_hosts"))
                agent = true
            }
        }
    }

}
