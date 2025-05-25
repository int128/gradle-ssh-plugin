package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.AtomicInteger

/**
 * A receiver thread reading lines from the stream.
 * It notifies events to {@link Listener} on received bytes.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Receiver implements Runnable {
    static final READ_BUFFER_SIZE = 1024 * 1024

    private static final sequenceForNaming = new AtomicInteger()

    final Stream stream
    final List<OutputStream> pipes = []

    private final Listener listener
    private final InputStream inputStream
    private final int id = sequenceForNaming.incrementAndGet()

    def Receiver(Listener listener1, Stream stream1, InputStream inputStream1) {
        listener = listener1
        stream = stream1
        inputStream = inputStream1
        assert listener
        assert stream
        assert inputStream
    }

    @Override
    void run() {
        try {
            log.trace("Started receiver $this")
            try {
                readStream()
            } catch (InterruptedIOException e) {
                log.debug("Interrupted receiver $this", e)
            } finally {
                inputStream.close()
            }
        } finally {
            log.trace("Finished receiver $this")
        }
    }

    private void readStream() {
        listener.start(stream)

        def readBuffer = new byte[READ_BUFFER_SIZE]
        while (!Thread.currentThread().interrupted) {
            log.trace("Waiting for $stream")
            def readLength = inputStream.read(readBuffer)
            if (readLength < 0) {
                log.trace("Reached end of stream on $stream")
                break
            }

            log.trace("Received $readLength bytes from $stream")
            pipes*.write(readBuffer, 0, readLength)
            listener.receive(stream, readBuffer, readLength)
        }

        listener.end(stream)
    }

    @Override
    String toString() {
        "${Receiver.simpleName}-${id}[${stream}]"
    }
}
