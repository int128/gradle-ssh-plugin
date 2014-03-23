package org.hidetake.gradle.ssh.internal.session.handler

import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.handler.FileTransfer

/**
 * A default implementation of {@link FileTransfer}.
 *
 * @author hidetake.org
 */
class DefaultFileTransfer implements FileTransfer {
    @Override
    void get(String remote, String local) {
        assert operations instanceof Operations
        operations.sftp {
            get(remote, local)
        }
    }

    @Override
    void put(String local, String remote) {
        assert operations instanceof Operations
        operations.sftp {
            put(local, remote)
        }
    }
}
