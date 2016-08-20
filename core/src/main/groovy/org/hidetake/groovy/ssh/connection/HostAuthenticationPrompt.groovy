package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.UserInfo
import groovy.util.logging.Slf4j

/**
 * An implementation of {@link UserInfo} for host key checking.
 * This should support prompt only for host key checking.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class HostAuthenticationPrompt implements UserInfo {

    @Override
    String getPassphrase() {
        throw new UnsupportedOperationException('UserInfo#getPassphrase()')
    }

    @Override
    String getPassword() {
        throw new UnsupportedOperationException('UserInfo#getPassword()')
    }

    @Override
    boolean promptPassword(String message) {
        throw new UnsupportedOperationException("UserInfo#promptPassword($message)")
    }

    @Override
    boolean promptPassphrase(String message) {
        throw new UnsupportedOperationException("UserInfo#promptPassphrase($message)")
    }

    @Override
    boolean promptYesNo(String message) {
        if (message.endsWith('Are you sure you want to continue connecting?')) {
            true
        } else if (message.endsWith('Do you want to delete the old key and insert the new key?')) {
            false
        } else {
            throw new UnsupportedOperationException("UserInfo#promptYesNo($message)")
        }
    }

    @Override
    void showMessage(String message) {
        throw new UnsupportedOperationException(message)
    }

}
