package org.hidetake.gradle.ssh.internal

import groovy.transform.TupleConstructor
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

/**
 * An implementation of {@link OutputStream} with logging facility.
 *
 * @author hidetake.org
 */
@TupleConstructor
class LoggingOutputStream extends OutputStream {
    final Logger logger
    final LogLevel logLevel

    private static final LINE_SEPARATOR = ~/\r\n|[\n\r\u2028\u2029\u0085]/
    private final buffer = new ByteArrayOutputStream(512)
    private lastBlock = ''

    /**
     * Line string filter.
     * Default is pass-through.
     */
    Closure<Boolean> filter = { String line -> true }

    /**
     * List of output lines.
     * Access this property after {@link #close()} in order to ensure last block is committed.
     */
    final List<String> lines = []

    void write(int b) {
        buffer.write(b)
    }

    /**
     * Flushes buffer.
     * Because it may be called regardless of line separators,
     * it should save last block and write it with next line.
     */
    void flush() {
        def block = lastBlock + new String(buffer.toByteArray())
        buffer.reset()

        def blockLines = LINE_SEPARATOR.split(block, Integer.MIN_VALUE).toList()
        if (!blockLines.empty) {
            lastBlock = blockLines.pop()
            blockLines.findAll(filter).each { writeLine it }
        }
    }

    void close() {
        flush()
        if (!lastBlock.empty && filter(lastBlock)) {
            writeLine lastBlock
        }
    }

    protected void writeLine(String line) {
        if (logger.isEnabled(logLevel)) {
            logger.log(logLevel, line)
        }
        lines << line
    }
}
