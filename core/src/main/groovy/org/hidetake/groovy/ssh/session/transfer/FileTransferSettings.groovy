package org.hidetake.groovy.ssh.session.transfer

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.SettingsHelper

trait FileTransferSettings {

    /**
     * File transfer method such as SFTP or SCP.
     */
    FileTransferMethod fileTransfer

    @EqualsAndHashCode
    static class With implements FileTransferSettings {
        def With() {}
        def With(FileTransferSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static FileTransferSettings DEFAULT = new FileTransferSettings.With(
                fileTransfer: FileTransferMethod.sftp
        )
    }

}
