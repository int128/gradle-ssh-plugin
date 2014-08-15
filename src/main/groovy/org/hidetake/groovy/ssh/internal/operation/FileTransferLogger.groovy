package org.hidetake.groovy.ssh.internal.operation

import com.jcraft.jsch.SftpProgressMonitor
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j

import java.text.NumberFormat

/**
 * A logger for monitoring progress of the file transfer.
 * This class shows following messages:
 * <ul>
 * <li>Start of transferring</li>
 * <li>Transferred bytes in each {@link FileTransferLogger#LOG_INTERVAL_MILLIS}</li>
 * <li>End of transferring</li>
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

    /**
     * Represents transferred bytes and elapsed time.
     */
    @TupleConstructor
    static class Status {
        /**
         * Estimated bytes to transfer.
         */
        final long maxSize = 0

        /**
         * Actually transferred bytes.
         */
        long transferredSize = 0

        /**
         * Timestamp in milliseconds when transfer is started.
         */
        final long startedTime = currentTime()

        /**
         * Timestamp in milliseconds when the last checkpoint is committed.
         */
        long lastCheckPointTime = currentTime()

        /**
         * Report the progress that data has been transferred.
         *
         * @param size transferred size in bytes
         * @return this instance
         */
        Status leftShift(long size) {
            transferredSize += size
            this
        }

        /**
         * Commit a checkpoint.
         */
        void checkPoint() {
            lastCheckPointTime = currentTime()
        }

        /**
         * Return percent of transferred data.
         *
         * @return percent of transferred data
         */
        double getPercent() {
            maxSize ? transferredSize / maxSize : 0
        }

        /**
         * Return transfer rate in kbps.
         *
         * @return transfer rate in kbps
         */
        double getKiloBytesPerSecond() {
            elapsedTime ? transferredSize / elapsedTime : 0
        }

        /**
         * Return elapsed time in milliseconds from started
         *
         * @return elapsed time in milliseconds from started
         */
        long getElapsedTime() {
            currentTime() - startedTime
        }

        /**
         * Return elapsed time in milliseconds from the last checkpoint
         *
         * @return elapsed time in milliseconds from the last checkpoint
         */
        long getElapsedTimeFromCheckPoint() {
            currentTime() - lastCheckPointTime
        }

        static long currentTime() {
            System.currentTimeMillis()
        }
    }
}
