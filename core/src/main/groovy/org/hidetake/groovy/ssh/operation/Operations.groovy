package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.forwarding.LocalPortForwardSettings
import org.hidetake.groovy.ssh.session.forwarding.RemotePortForwardSettings

/**
 * An aggregate of core SSH operations.
 *
 * @author Hidetake Iwata
 */
interface Operations {
    Remote getRemote()

    Operation shell(ShellSettings settings)

    Operation command(CommandSettings settings, String commandLine)

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
