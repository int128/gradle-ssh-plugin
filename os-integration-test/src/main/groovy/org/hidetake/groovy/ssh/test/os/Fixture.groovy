package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.core.Service

import static java.lang.System.getProperty
import static java.lang.System.getenv

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
                host = 'localhost'
                port = getenv('DOCKER_SSH_PORT') as Integer ?: 22
                user = getenv('DOCKER_SSH_USER') ?: getProperty('user.name')
                identity = new File("$buildDir/.ssh/id_rsa")
                knownHosts = addHostKey(new File("$buildDir/.ssh/known_hosts"))
            }
        }
        service.remotes {
            DefaultWithPassphrase {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                knownHosts = service.remotes.Default.knownHosts

                identity = new File("$buildDir/.ssh/id_rsa_passphrase")
                passphrase = 'pass1234'
            }
            DefaultWithOpenSSHKnownHosts {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                identity = service.remotes.Default.identity

                knownHosts = new File("$buildDir/.ssh/known_hosts_openssh")
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
