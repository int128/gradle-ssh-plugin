package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.core.Remote

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
     * @return result of the closure
     */
    def sftp(@DelegatesTo(SftpOperations) Closure closure)
}
