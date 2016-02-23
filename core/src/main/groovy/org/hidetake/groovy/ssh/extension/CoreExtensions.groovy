package org.hidetake.groovy.ssh.extension

/**
 * A set of extensions to be shipped as default.
 *
 * @author Hidetake Iwata
 */
trait CoreExtensions implements
        Command,
        BackgroundCommand,
        Shell,
        Sudo,
        SftpGet,
        SftpPut,
        SftpRemove,
        PortForward {
}
