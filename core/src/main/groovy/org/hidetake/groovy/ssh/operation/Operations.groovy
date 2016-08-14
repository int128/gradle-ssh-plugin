package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.forwarding.LocalPortForwardSettings
import org.hidetake.groovy.ssh.session.forwarding.RemotePortForwardSettings
import org.hidetake.groovy.ssh.session.transfer.FileTransferSettings

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

    def <T> T sftp(FileTransferSettings settings, @DelegatesTo(SftpOperations) Closure<T> closure)
}
