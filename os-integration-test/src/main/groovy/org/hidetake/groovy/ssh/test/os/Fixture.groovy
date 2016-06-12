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
                if (System.getenv('CI')) {
                    host = 'localhost'
                    user = System.getProperty('user.name')
                    identity = new File("$home/.ssh/id_rsa")
                } else {
                    host = System.getenv('EXT_SSH_HOST')
                    user = System.getenv('EXT_SSH_USER')
                    identity = new File("$home/.ssh/id_ext")
                    knownHosts = new File("$home/.ssh/known_hosts_ext")
                }
            }
            RequireAgent {
                host = System.getenv('EXT_SSH_HOST')
                user = System.getenv('EXT_SSH_USER')
                knownHosts = new File("$home/.ssh/known_hosts_ext")
                agent = true
            }
            RequireSudo {
                host = System.getenv('EXT_SSH_HOST')
                user = System.getenv('EXT_SSH_USER')
                identity = new File("$home/.ssh/id_ext")
                knownHosts = new File("$home/.ssh/known_hosts_ext")
            }
            RequireEcdsaUserKey {
                host = 'localhost'
                user = System.getProperty('user.name')
                identity = new File("$home/.ssh/id_ecdsa")
            }
            RequireEcdsaHostKey {
                host = 'localhost'
                user = System.getProperty('user.name')
                identity = new File("$home/.ssh/id_rsa")
                knownHosts = new File("$home/.ssh/known_hosts_ecdsa")
            }
            RequireKeyWithPassphrase {
                host = 'localhost'
                user = System.getProperty('user.name')
                identity = new File("$home/.ssh/id_rsa_passphrase")
                passphrase = 'pass1234'
            }
        }
    }

}
