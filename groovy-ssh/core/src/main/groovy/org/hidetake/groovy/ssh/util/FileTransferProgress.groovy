package org.hidetake.groovy.ssh.util

import groovy.transform.TupleConstructor

import java.text.NumberFormat

/**
 * A logger for monitoring progress of the file transfer.
 * This class shows following messages:
 * <ul>
 * <li>Start of transferring</li>
 * <li>Transferred bytes in each {@link FileTransferProgress#LOG_INTERVAL_MILLIS}</li>
 * <li>End of transferring</li>
 *
 * @author Hidetake Iwata
 */
class FileTransferProgress {
    protected static final LOG_INTERVAL_MILLIS = 2000L

    protected Status status

    private final Closure notifier
    private final percentFormat = NumberFormat.percentInstance

    FileTransferProgress(Closure notifier1 = { percent -> }) {
        notifier = notifier1
    }

    FileTransferProgress(long size, Closure notifier1 = { percent -> }) {
        notifier = notifier1
        reset(size)
    }

    void reset(long size) {
        status = new Status(size)
    }

    void report(long size) {
        status << size
        if (status.elapsedTimeFromCheckPoint > LOG_INTERVAL_MILLIS) {
            status.checkPoint()
            notifier.call(percentFormat.format(status.percent))
        }
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
