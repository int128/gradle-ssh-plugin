package org.hidetake.groovy.ssh.interaction

/**
 * An aggregation class of streams and receiver threads.
 *
 * @author Hidetake Iwata
 */
class Interactions {
    private final OutputStream standardInput

    private final Listener listener
    private final List<Receiver> receivers = []
    private final List<Thread> threads = []
    private final List<Throwable> exceptions = [].asSynchronized()

    private final uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        void uncaughtException(Thread t, Throwable e) {
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
    def Interactions(OutputStream standardInput1, InputStream standardOutput, InputStream standardError, String encoding) {
        standardInput = standardInput1
        listener = new Listener()
        receivers.add(new Receiver(listener, Stream.StandardOutput, standardOutput, encoding))
        receivers.add(new Receiver(listener, Stream.StandardError, standardError, encoding))
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
    def Interactions(OutputStream standardInput1, InputStream standardOutput, String encoding) {
        standardInput = standardInput1
        listener = new Listener()
        receivers.add(new Receiver(listener, Stream.StandardOutput, standardOutput, encoding))
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
        listener.add(new Processor(closure, standardInput))
    }

    /**
     * Starts receiver threads.
     */
    void start() {
        threads.addAll(receivers.collect { new Thread(it) })
        threads*.uncaughtExceptionHandler = uncaughtExceptionHandler

        exceptions.clear()
        listener.start()

        threads*.start()
    }

    /**
     * Waits for all receiver threads.
     */
    void waitForEndOfStream() {
        threads*.join()
        if (!exceptions.empty) {
            throw new InteractionException(*exceptions)
        }
    }
}
