package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * An aggregation class of streams and receiver threads.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Interactions {
    private final OutputStream standardInput
    private final String encoding

    private final Listener listener = new Listener()
    private final List<Receiver> receivers = []
    private final List<Thread> threads = []
    private final List<Throwable> exceptions = [].asSynchronized()

    private final uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        void uncaughtException(Thread t, Throwable e) {
            log.debug("Uncaught exception at $t", e)
            exceptions.add(e)
        }
    }

    /**
     * Constructor.
     * All streams will be closed by the receiver thread.
     *
     * @param standardInput1
     * @param standardOutput
     * @param standardError
     * @param encoding
     * @return
     */
    def Interactions(OutputStream standardInput1, InputStream standardOutput, InputStream standardError, String encoding1) {
        standardInput = standardInput1
        encoding = encoding1
        receivers.add(new Receiver(listener, Stream.StandardOutput, standardOutput))
        receivers.add(new Receiver(listener, Stream.StandardError, standardError))
    }

    /**
     * Constructor.
     * All streams will be closed by the receiver thread.
     *
     * @param standardInput1
     * @param standardOutput
     * @param encoding
     * @return
     */
    def Interactions(OutputStream standardInput1, InputStream standardOutput, String encoding1) {
        standardInput = standardInput1
        encoding = encoding1
        receivers.add(new Receiver(listener, Stream.StandardOutput, standardOutput))
    }

    /**
     * Pipes the stream into another stream.
     *
     * @param stream
     * @param outputStream
     */
    void pipe(Stream stream, OutputStream outputStream) {
        receivers.find { it.stream == stream }.pipes.add(outputStream)
    }

    /**
     * Adds an interaction.
     *
     * @param closure definition of interaction
     */
    void add(@DelegatesTo(InteractionHandler) Closure closure) {
        listener.add(new Processor(closure, standardInput, encoding))
    }

    /**
     * Starts receiver threads.
     */
    void start() {
        threads.addAll(receivers.collect { new Thread(it, it.toString()) })
        threads*.uncaughtExceptionHandler = uncaughtExceptionHandler

        exceptions.clear()

        threads*.start()
    }

    /**
     * Waits for all receiver threads.
     */
    void waitForEndOfStream() {
        log.debug("Waiting for interaction threads: $threads")
        threads*.join()
        log.debug("Terminated interaction threads: $threads")
        if (!exceptions.empty) {
            throw new InteractionException(*exceptions)
        }
    }
}
