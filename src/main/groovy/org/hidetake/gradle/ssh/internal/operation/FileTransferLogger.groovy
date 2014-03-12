package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.SftpProgressMonitor
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j

import static java.lang.String.format

/**
 * Logger for file transfer.
 *
 * @author hidetake.org
 */
@Slf4j
class FileTransferLogger implements SftpProgressMonitor {
    protected static final LOG_INTERVAL_MILLIS = 3000L

    protected Status status

    @Override
    void init(int op, String src, String dest, long max) {
        status = new Status(max)
        log.info("Starting transfer ${formatBytes(status.maxSize)}.")
    }

    @Override
    boolean count(long count) {
        status << count
        if (status.elapsedTimeFromCheckPoint > LOG_INTERVAL_MILLIS) {
            status.checkPoint()
            log.info("Transferred ${status.percent}% in ${formatTime(status.elapsedTime)}.")
        }
        true
    }

    @Override
    void end() {
        log.info("Finished transfer ${formatBytes(status.transferredSize)} " +
                 "(${formatKBytes(status.bytesPerSecond)}/s). " +
                 "Took ${formatTime(status.elapsedTime)}.")
    }

    private static formatBytes(long number) {
        format('%,d bytes', number)
    }

    private static formatKBytes(long number) {
        format('%,d kB', number / 1000 as long)
    }

    private static formatTime(long number) {
        format('%.3f secs', number / 1000.0 as double)
    }

    @TupleConstructor
    static class Status {
        final long maxSize = 0
        long transferredSize = 0

        final long startedTime = currentTime()
        long lastCheckPointTime = currentTime()

        Status leftShift(long size) {
            transferredSize += size
            this
        }

        int getPercent() {
            maxSize ? 100 * transferredSize / maxSize : 0
        }

        long getBytesPerSecond() {
            1000 * transferredSize / elapsedTime
        }

        long getElapsedTime() {
            currentTime() - startedTime
        }

        void checkPoint() {
            lastCheckPointTime = currentTime()
        }

        long getElapsedTimeFromCheckPoint() {
            currentTime() - lastCheckPointTime
        }

        static long currentTime() {
            System.currentTimeMillis()
        }
    }
}
