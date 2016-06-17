package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.experimental.categories.Category
import spock.lang.Specification
import spock.lang.Unroll

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes

/**
 * Check if {@link org.hidetake.groovy.ssh.session.execution.Sudo} works with Linux system.
 *
 * @author Hidetake Iwata
 */
@Category(RequireSudo)
class SudoSpec extends Specification {

    private static final privilegeUser = 'groovyssh'
    private static final anotherUser = 'groovyssh2'

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    @Unroll
    def 'should execute a privileged command on #sudoSpec'() {
        given:
        recreateUser(privilegeUser)
        configurePassword(privilegeUser, 'passForPrivilegeUser')
        configureAuthorizedKeys(privilegeUser)
        configureSudo(privilegeUser, sudoSpec)

        and:
        ssh.remotes {
            PrivilegedUser {
                host = ssh.remotes.RequireSudo.host
                identity = ssh.remotes.RequireSudo.identity
                knownHosts = ssh.remotes.RequireSudo.knownHosts
                user = privilegeUser
            }
        }

        when:
        def whoami = ssh.run {
            session(ssh.remotes.PrivilegedUser) {
                executeSudo 'whoami', pty: true, sudoPassword: 'passForPrivilegeUser'
            }
        }

        then:
        whoami == 'root'

        where:
        sudoSpec << ['ALL=(ALL) ALL', 'ALL=(ALL) NOPASSWD: ALL']
    }

    @Unroll
    def 'should execute a command as another user on #sudoSpec'() {
        given:
        recreateUser(privilegeUser)
        configurePassword(privilegeUser, 'passForPrivilegeUser')
        configureAuthorizedKeys(privilegeUser)

        recreateUser(anotherUser)
        configureAuthorizedKeys(anotherUser)

        and:
        ssh.remotes {
            PrivilegedUser {
                host = ssh.remotes.RequireSudo.host
                identity = ssh.remotes.RequireSudo.identity
                knownHosts = ssh.remotes.RequireSudo.knownHosts
                user = privilegeUser
            }
        }

        when:
        def whoami = ssh.run {
            session(ssh.remotes.PrivilegedUser) {
                executeSudo "-u $anotherUser whoami", pty: true, sudoPassword: 'passForPrivilegeUser'
            }
        }

        then:
        whoami == anotherUser

        where:
        sudoSpec << ['ALL=(ALL) ALL', 'ALL=(ALL) NOPASSWD: ALL']
    }

    private recreateUser(String user) {
        ssh.run {
            session(ssh.remotes.RequireSudo) {
                execute """
                    if id "$user"; then
                        sudo userdel -r $user
                    fi
                    sudo useradd -m $user
                """, pty: true
            }
        }
    }

    private configurePassword(String user, String password) {
        ssh.run {
            session(ssh.remotes.RequireSudo) {
                execute "sudo passwd $user", pty: true, interaction: {
                    when(partial: ~/.+[Pp]assword: */) {
                        standardInput << password << '\n'
                    }
                    when(line: _) {}
                }
            }
        }
    }

    private configureAuthorizedKeys(String user) {
        ssh.run {
            session(ssh.remotes.RequireSudo) {
                execute """
                    sudo -i -u $user mkdir -m 700 .ssh
                    sudo -i -u $user touch .ssh/authorized_keys
                    sudo -i -u $user chmod 600 .ssh/authorized_keys
                    sudo -i -u $user tee .ssh/authorized_keys < ~/.ssh/authorized_keys > /dev/null
                """, pty: true
            }
        }
    }

    private configureSudo(String user, String spec) {
        ssh.run {
            session(ssh.remotes.RequireSudo) {
                put text: "$user $spec", into: "/tmp/$user"
                execute """
                    sudo chmod 440 /tmp/$user
                    sudo chown 0.0 /tmp/$user
                    sudo mkdir -p -m 700 /etc/sudoers.d
                    sudo mv /tmp/$user /etc/sudoers.d/$user
                """, pty: true
            }
        }
    }

}
