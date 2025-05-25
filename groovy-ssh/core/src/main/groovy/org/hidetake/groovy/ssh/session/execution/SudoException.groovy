package org.hidetake.groovy.ssh.session.execution

import groovy.transform.CompileStatic
import org.hidetake.groovy.ssh.core.Remote

/**
 * An exception class thrown if sudo authentication failed.
 *
 * @author Hidetake Iwata
 */
@CompileStatic
class SudoException extends RuntimeException {

    final Remote remote

    def SudoException(Remote remote, String message) {
        super("Failed sudo authentication on $remote: $message")
        this.remote = remote
    }

}
