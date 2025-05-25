package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.SftpProgressMonitor
import groovy.transform.CompileStatic
import org.hidetake.groovy.ssh.util.FileTransferProgress

/**
 * A bridge class between {@link SftpProgressMonitor} and {@link FileTransferProgress}.
 *
 * Use {@link CompileStatic} to prevent {@link IncompatibleClassChangeError} on Gradle 1.x.
 *
 * @author Hidetake Iwata
 */
@CompileStatic
class SftpProgress extends FileTransferProgress implements SftpProgressMonitor {

    def SftpProgress(Closure notifier) {
        super(notifier)
    }

    @Override
    void init(int op, String src, String dest, long max) {
        reset(max)
    }

    @Override
    boolean count(long count) {
        report(count)
        true
    }

    @Override
    void end() {
    }

}
