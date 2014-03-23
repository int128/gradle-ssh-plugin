package org.hidetake.gradle.ssh.internal.operation.interaction

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j

/**
 * An implementation of line oriented {@link OutputStream}.
 *
 * @author hidetake.org
 */
@TupleConstructor
@Slf4j
class LineOutputStream extends OutputStream {
    final String charset = 'UTF-8'

    /**
     * Listeners for line processing.
     *
     * Called when the stream received a line.
     * The stream must call this when received an new-line character or closed.
     */
    final List<Closure> lineListeners = []

    /**
     * Listeners for partial matching to the line buffer.
     *
     * Called when the stream is flushed.
     * This method may be called several times until the stream receives a complete line.
     *
     * If at least one of closures returned true,
     * line buffer will be cleared and logging listeners (see below) will be called.
     * Otherwise, it will be preserved until new-line appears.
     */
    final List<Closure<Boolean>> partialListeners = []

    /**
     * Listeners for logging output.
     *
     * Called when the stream received a line or
     * at least one of partial listeners returned true.
     */
    final List<Closure> loggingListeners = []

    private static final LINE_SEPARATOR = ~/\r\n|[\n\r\u2028\u2029\u0085]/
    private final byteBuffer = new ByteArrayOutputStream(512)
    private lineBuffer = ''

    void write(int b) {
        withTryCatch {
            byteBuffer.write(b)
        }
    }

    /**
     * Flush the buffer.
     * This method may be called regardless of line separators,
     * so it saves last block and write it in the next chance.
     */
    void flush() {
        withTryCatch {
            def receivedBlock = new String(byteBuffer.toByteArray(), charset)
            byteBuffer.reset()

            def cumulativeBlock = lineBuffer + receivedBlock
            def lines = LINE_SEPARATOR.split(cumulativeBlock, Integer.MIN_VALUE)
            if (lines.length > 0) {
                lines.take(lines.length - 1).each {
                    lineListeners*.call(it)
                    loggingListeners*.call(it)
                }

                lineBuffer = lines.last()
                if (receivedBlock && lineBuffer) {
                    log.debug("Trying partial match for '$lineBuffer'")
                    def partialHit = partialListeners*.call(lineBuffer)
                    if (partialHit.any()) {
                        log.debug('Partial match hit. Line buffer cleared.')
                        loggingListeners*.call(lineBuffer)
                        lineBuffer = ''
                    } else {
                        log.debug("No partial match hit.")
                    }
                }
            }
        }
    }

    void close() {
        withTryCatch {
            def receivedBlock = new String(byteBuffer.toByteArray(), charset)
            byteBuffer.reset()

            def cumulativeBlock = lineBuffer + receivedBlock
            def lines = LINE_SEPARATOR.split(cumulativeBlock, Integer.MIN_VALUE)
            if (lines.length > 0) {
                lines.take(lines.length - 1).each {
                    lineListeners*.call(it)
                    loggingListeners*.call(it)
                }

                lineBuffer = lines.last()
                if (lineBuffer) {
                    lineListeners*.call(lineBuffer)
                    loggingListeners*.call(lineBuffer)
                    lineBuffer = ''
                }
            }
        }
    }

    private static withTryCatch(Closure closure) {
        try {
            closure()
        } catch (Throwable throwable) {
            log.error("Error while processing stream: ${throwable}")
            log.debug("Error while processing stream: ", throwable)
            throw new IOException(throwable)
        }
    }
}
