package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings

/**
 * An aggregate of core SSH operations.
 *
 * @author Hidetake Iwata
 */
interface Operations {
    Remote getRemote()

    void shell(OperationSettings settings)

    String execute(OperationSettings settings, String command, Closure callback)

    void executeBackground(OperationSettings settings, String command, Closure callback)

    int forwardLocalPort(LocalPortForwardSettings settings)

    void forwardRemotePort(RemotePortForwardSettings settings)

    /**
     * Perform SFTP operations.
     *
     * @param closure closure for {@link SftpOperations}
     * @return result of the closure
     */
    def sftp(@DelegatesTo(SftpOperations) Closure closure)
}
