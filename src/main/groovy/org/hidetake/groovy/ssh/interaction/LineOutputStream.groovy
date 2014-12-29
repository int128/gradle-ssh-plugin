package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * An implementation of line oriented {@link OutputStream}.
 *
 * @author hidetake.org
 */
@Slf4j
class LineOutputStream extends OutputStream {
    private static final LINE_SEPARATOR = ~/\r\n|[\n\r\u2028\u2029\u0085]/

    private final String charset

    private final List<Closure> lineListeners = []
    private final List<Closure<Boolean>> partialListeners = []
    private final List<Closure> loggingListeners = []
    private final List<OutputStream> linkedStreams = []

    private final byteBuffer = new ByteArrayOutputStream(512)
    private lineBuffer = ''

    def LineOutputStream(String charset1 = 'UTF-8') {
        charset = charset1
        assert charset
    }

    /**
     * Add a listener for line processing.
     *
     * Called when the stream received a line.
     * The stream must call this when received an new-line character or closed.
     *
     * @param closure
     */
    void listenLine(Closure closure) {
        lineListeners.add(closure)
    }

    /**
     * Add a listener for partial matching to the line buffer.
     *
     * Called when the stream is flushed.
     * This method may be called several times until the stream receives a complete line.
     *
     * If at least one of closures returned true,
     * line buffer will be cleared and logging listeners (see below) will be called.
     * Otherwise, it will be preserved until new-line appears.
     *
     * @param closure
     */
    void listenPartial(Closure<Boolean> closure) {
        partialListeners.add(closure)
    }

    /**
     * Add a listeners for logging output.
     *
     * Called when the stream received a line or
     * at least one of partial listeners returned true.
     *
     * @param closure
     */
    void listenLogging(Closure closure) {
        loggingListeners.add(closure)
    }

    /**
     * Link the stream.
     * A byte received by {@link #write(int)} will be written to the stream.
     *
     * @param stream the output stream
     */
    void linkStream(OutputStream stream) {
        linkedStreams.add(stream)
    }

    void write(int b) {
        withTryCatch {
            byteBuffer.write(b)
            linkedStreams*.write(b)
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
