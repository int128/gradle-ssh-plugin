package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.session.SessionExtension

trait UserManagementExtension implements SessionExtension {

    void recreateUser(String user) {
        execute """
            if id "$user"; then
                sudo userdel -r $user
            fi
            sudo useradd -m $user
        """, pty: true
    }

    void configurePassword(String user, String password) {
        execute "sudo passwd $user", pty: true, interaction: Helper.passwordInteraction.curry(password)
    }

    private static class Helper {
        static final passwordInteraction = { password ->
            when(partial: ~/.+[Pp]assword: */) {
                standardInput << password << '\n'
            }
            when(line: _) {}
        }
    }

    void configureAuthorizedKeysAsCurrentUser(String user) {
        execute """
            sudo -i -u $user mkdir -m 700 .ssh
            sudo -i -u $user touch .ssh/authorized_keys
            sudo -i -u $user chmod 600 .ssh/authorized_keys
            sudo -i -u $user tee .ssh/authorized_keys < ~/.ssh/authorized_keys > /dev/null
        """, pty: true
    }

    void configureAuthorizedKeys(String user, String publicKey) {
        execute """
            sudo -i -u $user mkdir -m 700 .ssh
            sudo -i -u $user touch .ssh/authorized_keys
            sudo -i -u $user chmod 600 .ssh/authorized_keys
            echo '$publicKey' | sudo -i -u $user tee .ssh/authorized_keys > /dev/null
        """, pty: true
    }

    void configureSudo(String user, String spec) {
        put text: "$user $spec", into: "/tmp/$user"
        execute """
            sudo chmod 440 /tmp/$user
            sudo chown 0.0 /tmp/$user
            sudo mkdir -p -m 700 /etc/sudoers.d
            sudo mv /tmp/$user /etc/sudoers.d/$user
        """, pty: true
    }

}
