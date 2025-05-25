package org.hidetake.groovy.ssh.session.transfer

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.SettingsHelper

trait FileTransferSettings {

    /**
     * File transfer method such as SFTP or SCP.
     */
    FileTransferMethod fileTransfer

    /**
     * Timeout for the SFTP or command channel to be connected in seconds.
     * @see org.hidetake.groovy.ssh.connection.ConnectionSettings#timeoutSec
     */
    Integer timeoutSec


    @EqualsAndHashCode
    static class With implements FileTransferSettings {
        def With() {}
        def With(FileTransferSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static FileTransferSettings DEFAULT = new FileTransferSettings.With(
            fileTransfer: FileTransferMethod.sftp,
            timeoutSec: 0,
        )
    }

}
