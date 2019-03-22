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
        service.remotes {
            Default {
                host = 'localhost'
                port = 22
                user = 'tester'
                identity = new File("etc/ssh/id_rsa")
                knownHosts = addHostKey(new File("build/known_hosts"))
            }
        }
        service.remotes {
            DefaultWithECDSAKey {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                identity = new File("etc/ssh/id_ecdsa")
                knownHosts = service.remotes.Default.knownHosts
            }
            DefaultWithPassphrase {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                knownHosts = service.remotes.Default.knownHosts

                identity = new File("etc/ssh/id_rsa_pass")
                passphrase = 'gradle'
            }
            DefaultWithOpenSSHKnownHosts {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                identity = service.remotes.Default.identity

                knownHosts = new File("etc/ssh/known_hosts")
            }
            DefaultWithAgent {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                knownHosts = service.remotes.Default.knownHosts

                agent = true
            }
        }
    }

}
