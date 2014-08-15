package org.hidetake.groovy.ssh.api.operation

import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.api.Remote

/**
 * An aggregate of core SSH operations.
 *
 * @author hidetake.org
 */
interface Operations {
    Remote getRemote()

    void shell(OperationSettings settings)

    String execute(OperationSettings settings, String command, Closure callback)

    void executeBackground(OperationSettings settings, String command, Closure callback)

    /**
     * Perform SFTP operations.
     *
     * @param closure closure for {@link SftpOperations}
     */
    void sftp(Closure closure)
}
