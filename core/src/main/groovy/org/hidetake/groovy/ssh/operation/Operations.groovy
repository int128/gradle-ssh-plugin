package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings

/**
 * An aggregate of core SSH operations.
 *
 * @author Hidetake Iwata
 */
interface Operations {
    Remote getRemote()

    Operation shell(OperationSettings settings)

    Operation command(OperationSettings settings, String commandLine)

    int forwardLocalPort(LocalPortForwardSettings settings)

    void forwardRemotePort(RemotePortForwardSettings settings)

    /**
     * Perform SFTP operations.
     *
     * @param closure closure for {@link SftpOperations}
     * @return result of the closure
     */
    def <T> T sftp(@DelegatesTo(SftpOperations) Closure<T> closure)
}
