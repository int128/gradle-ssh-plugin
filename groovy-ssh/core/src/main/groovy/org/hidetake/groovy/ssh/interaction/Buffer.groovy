package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * A byte buffer which supports last-in and first-out.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Buffer {

    private final String encoding

    private final buffer = new ByteArrayOutputStream(Receiver.READ_BUFFER_SIZE)

    def Buffer(String encoding1) {
        encoding = encoding1
    }

    /**
     * Append bytes to last.
     *
     * @param bytes
     * @param length
     */
    void append(byte[] bytes, int length) {
        buffer.write(bytes, 0, length)
        log.trace("Appended $length bytes to buffer, now ${size()} bytes")
    }

    /**
     * Append bytes to last.
     *
     * @param bytes
     */
    void append(byte[] bytes) {
        append(bytes, bytes.length)
    }

    /**
     * Append string to last.
     *
     * @param string
     */
    void append(String string) {
        append(string.getBytes(encoding))
    }

    /**
     * Drop first bytes.
     * If {@param length} is greater than buffer size, whole is dropped.
     *
     * @param length must be greater than 0
     * @return dropped bytes
     */
    byte[] dropBytes(int length) {
        assert length > 0, 'can not drop 0 or minus byte'
        def original = buffer.toByteArray()
        buffer.reset()
        if (length < original.length) {
            buffer.write(original, length, original.length - length)
            log.trace("Dropped first $length bytes of buffer, now ${size()} bytes")
            Arrays.copyOf(original, length)
        } else {
            log.trace("Dropped whole buffer of $original.length bytes (requested $length bytes)")
            original
        }
    }

    /**
     * Drop first bytes of given characters length.
     *
     * @param string
     */
    void dropChars(String string) {
        def length = string.getBytes(encoding).length
        assert length > 0, 'can not drop 0 byte'
        def original = buffer.toByteArray()
        buffer.reset()
        if (length < original.length) {
            buffer.write(original, length, original.length - length)
            log.trace("Dropped first $length bytes of buffer, now ${size()} bytes")
        } else {
            log.trace("Dropped whole buffer of $original.length bytes (requested $length bytes)")
        }
    }

    /**
     * Return size of the buffer.
     * @return
     */
    int size() {
        buffer.size()
    }

    /**
     * Return string representation of the buffer.
     * @return
     */
    String toString() {
        buffer.toString(encoding)
    }

}
