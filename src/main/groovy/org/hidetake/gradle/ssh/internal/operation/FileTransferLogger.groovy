package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.SftpProgressMonitor
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j

import java.text.NumberFormat

/**
 * Logger for file transfer.
 *
 * @author hidetake.org
 */
@Slf4j
class FileTransferLogger implements SftpProgressMonitor {
    protected static final LOG_INTERVAL_MILLIS = 3000L

    protected Status status

    private final bytesFormat = NumberFormat.integerInstance
    private final percentFormat = NumberFormat.percentInstance
    private final timeFormat = NumberFormat.numberInstance

    FileTransferLogger() {
        timeFormat.maximumFractionDigits = 3
    }

    @Override
    void init(int op, String src, String dest, long max) {
        status = new Status(max)
        log.info("Starting transfer ${bytesFormat.format(status.maxSize)} bytes.")
    }

    @Override
    boolean count(long count) {
        status << count
        if (status.elapsedTimeFromCheckPoint > LOG_INTERVAL_MILLIS) {
            status.checkPoint()
            log.info("Transferred ${percentFormat.format(status.percent)} " +
                     "in ${timeFormat.format(status.elapsedTime / 1000.0)} secs.")
        }
        true
    }

    @Override
    void end() {
        log.info("Finished transfer ${bytesFormat.format(status.transferredSize)} bytes " +
                 "(${bytesFormat.format(status.kiloBytesPerSecond)} kB/s). " +
                 "Took ${timeFormat.format(status.elapsedTime / 1000.0)} secs.")
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

        double getPercent() {
            maxSize ? transferredSize / maxSize : 0
        }

        double getKiloBytesPerSecond() {
            transferredSize / elapsedTime
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
