package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * A receiver thread reading lines from the stream.
 * This calls {@link Listener} on each line or partial string.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Receiver implements Runnable {
    private static final LINE_SEPARATOR = ~/\r\n|[\n\r\u2028\u2029\u0085]/

    final Stream stream
    final List<OutputStream> pipes = []

    private final Listener listener
    private final InputStream inputStream
    private final String encoding

    private lineBuffer = ''

    def Receiver(Listener listener1, Stream stream1, InputStream inputStream1, String encoding1) {
        listener = listener1
        stream = stream1
        inputStream = inputStream1
        encoding = encoding1
        assert listener
        assert stream
        assert inputStream
        assert encoding
    }

    @Override
    void run() {
        log.debug("Started receiver for $stream")
        try {
            readStream()
        } finally {
            inputStream.close()
        }
    }

    private void readStream() {
        def readBuffer = new byte[1024]
        def byteBuffer = new ByteArrayOutputStream(1024)
        while (true) {
            def readLength = inputStream.read(readBuffer)
            if (readLength < 0) {
                break
            }

            log.trace("Received $readLength bytes from $stream")
            pipes*.write(readBuffer, 0, readLength)
            byteBuffer.write(readBuffer, 0, readLength)
            def block = new String(byteBuffer.toByteArray(), encoding)
            byteBuffer.reset()
            onReceivedBlock(block)
        }

        log.trace("Reached end of stream on $stream")
        onEndOfStream()
    }

    private void onReceivedBlock(String block) {
        def cumulativeBlock = lineBuffer + block
        def lines = LINE_SEPARATOR.split(cumulativeBlock, Integer.MIN_VALUE)
        if (lines.length > 0) {
            log.trace("Received $lines.length lines from $stream")
            lines.take(lines.length - 1).each { line ->
                listener.processLine(stream, line)
            }

            lineBuffer = lines.last()
            if (block && lineBuffer) {
                listener.processPartial(stream, lineBuffer)
            }
        }
    }

    private void onEndOfStream() {
        def cumulativeBlock = lineBuffer
        def lines = LINE_SEPARATOR.split(cumulativeBlock, Integer.MIN_VALUE)
        if (lines.length > 0) {
            log.trace("Received $lines.length lines from $stream")
            lines.take(lines.length - 1).each { line ->
                listener.processLine(stream, line)
            }

            lineBuffer = lines.last()
            if (lineBuffer) {
                listener.processLine(stream, lineBuffer)
                lineBuffer = ''
            }
        }
    }
}
