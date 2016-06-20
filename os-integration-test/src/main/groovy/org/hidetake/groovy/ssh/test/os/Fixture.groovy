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
        final home = System.getProperty('user.home')
        service.remotes {
            Default {
                host = System.getenv('EXT_SSH_HOST')
                user = System.getenv('EXT_SSH_USER')
                identity = new File("$home/.ssh/id_ext")
                knownHosts = new File("$home/.ssh/known_hosts_ext")
            }
            DefaultWithPassphrase {
                host = System.getenv('EXT_SSH_HOST')
                user = System.getenv('EXT_SSH_USER')
                identity = new File("$home/.ssh/id_ext_passphrase")
                passphrase = 'pass1234'
                knownHosts = new File("$home/.ssh/known_hosts_ext")
            }
            DefaultWithEcdsaHostKey {
                host = System.getenv('EXT_SSH_HOST')
                user = System.getenv('EXT_SSH_USER')
                identity = new File("$home/.ssh/id_ext")
                knownHosts = new File("$home/.ssh/known_hosts_ext_ecdsa")
            }
            DefaultWithAgent {
                host = System.getenv('EXT_SSH_HOST')
                user = System.getenv('EXT_SSH_USER')
                knownHosts = new File("$home/.ssh/known_hosts_ext")
                agent = true
            }
        }
    }

}
