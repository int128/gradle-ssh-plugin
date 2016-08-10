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
                identity = new File("$buildDir/id_rsa")
                knownHosts = new File("$buildDir/known_hosts")
            }
            DefaultWithPassphrase {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                identity = new File("$buildDir/id_rsa_passphrase")
                passphrase = 'pass1234'
                knownHosts = new File("$buildDir/known_hosts")
            }
            DefaultWithEcdsaHostKey {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                identity = new File("$buildDir/id_rsa")
                knownHosts = new File("$buildDir/known_hosts_ecdsa")
            }
            DefaultWithAgent {
                host = 'sandbox.127.0.0.1.xip.io'
                port = 8022
                user = 'tester'
                knownHosts = new File("$buildDir/known_hosts")
                agent = true
            }
        }
    }

}
