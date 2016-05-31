package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.session.execution.BackgroundCommand
import org.hidetake.groovy.ssh.session.execution.Command
import org.hidetake.groovy.ssh.session.execution.Shell
import org.hidetake.groovy.ssh.session.execution.Sudo
import org.hidetake.groovy.ssh.session.forwarding.PortForward
import org.hidetake.groovy.ssh.session.transfer.FileGet
import org.hidetake.groovy.ssh.session.transfer.FilePut
import org.hidetake.groovy.ssh.session.transfer.SftpRemove

/**
 * A set of extensions to be shipped as default.
 *
 * @author Hidetake Iwata
 */
trait SessionExtensions implements
        Command,
        BackgroundCommand,
        Shell,
        Sudo,
        FileGet,
        FilePut,
        SftpRemove,
        PortForward {
}
