package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.experimental.categories.Category
import spock.lang.Specification
import spock.lang.Unroll

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemote
import static org.hidetake.groovy.ssh.test.os.Fixture.randomInt

/**
 * Check if {@link org.hidetake.groovy.ssh.extension.Sudo} works with Linux system.
 * Password authentication must be enabled on sshd_config to run tests.
 *
 * @author Hidetake Iwata
 */
@Category(RequireSudo)
class SudoSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        createRemote(ssh, 'testServer')
    }

    @Unroll
    def 'should execute a privileged command on #sudoSpec'() {
        given:
        def sudoUser = "user${randomInt()}"
        def sudoPassword = UUID.randomUUID().toString()
        createPrivilegedUser(sudoUser, sudoPassword, sudoSpec)
        ssh.remotes {
            sudoHost {
                host = ssh.remotes.testServer.host
                user = sudoUser
                password = sudoPassword
            }
        }

        when:
        def whoami = ssh.run {
            session(ssh.remotes.sudoHost) {
                executeSudo('whoami', pty: true)
            }
        }

        then:
        whoami == 'root'

        cleanup:
        deletePrivilegedUser(sudoUser)

        where:
        sudoSpec << ['ALL=(ALL) ALL', 'ALL=(ALL) NOPASSWD: ALL']
    }

    @Unroll
    def 'should execute a command as another user on #sudoSpec'() {
        given:
        def sudoUser = "user${randomInt()}"
        def sudoPassword = UUID.randomUUID().toString()
        createPrivilegedUser(sudoUser, sudoPassword, sudoSpec)
        ssh.remotes {
            sudoHost {
                host = ssh.remotes.testServer.host
                user = sudoUser
                password = sudoPassword
            }
        }

        and:
        def anotherUser = "another${randomInt()}"
        ssh.run {
            session(ssh.remotes.testServer) {
                execute("sudo useradd -m $anotherUser", pty: true)
            }
        }

        when:
        def whoami = ssh.run {
            session(ssh.remotes.sudoHost) {
                executeSudo("-u $anotherUser whoami", pty: true)
            }
        }

        then:
        whoami == anotherUser

        cleanup:
        deletePrivilegedUser(sudoUser)
        ssh.run {
            session(ssh.remotes.testServer) {
                execute("sudo userdel -r $anotherUser", pty: true)
            }
        }

        where:
        sudoSpec << ['ALL=(ALL) ALL', 'ALL=(ALL) NOPASSWD: ALL']
    }

    private createPrivilegedUser(String sudoUser, String sudoPassword, String sudoSpec) {
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'sudo test -d /etc/sudoers.d', pty: true
                execute "sudo useradd -m $sudoUser", pty: true
                execute "sudo passwd $sudoUser", pty: true, interaction: {
                    when(partial: ~/.+[Pp]assword: */) {
                        standardInput << sudoPassword << '\n'
                    }
                }
                put text: "$sudoUser $sudoSpec", into: "/tmp/$sudoUser"
                execute "sudo chmod 440 /tmp/$sudoUser", pty: true
                execute "sudo chown 0.0 /tmp/$sudoUser", pty: true
                execute "sudo mv /tmp/$sudoUser /etc/sudoers.d/$sudoUser", pty: true
            }
        }
    }

    private deletePrivilegedUser(String sudoUser) {
        ssh.run {
            session(ssh.remotes.testServer) {
                execute "sudo rm -v /etc/sudoers.d/$sudoUser", pty: true
                execute "sudo userdel -r $sudoUser", pty: true
            }
        }
    }

}
