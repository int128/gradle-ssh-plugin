package org.hidetake.groovy.ssh.extension.settings;

/**
 * File transfer method type.
 * Implemented as Java native enum for Gradle 1.x compatibility.
 *
 * @author Hidetake Iwata
 */
public enum FileTransferMethod {
    /**
     * Transfer via SFTP channel
     */
    sftp,

    /**
     * Transfer via SCP command
     */
    scp
}
