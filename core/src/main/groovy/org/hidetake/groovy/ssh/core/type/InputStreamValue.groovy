package org.hidetake.groovy.ssh.core.type

/**
 * A value object for
 * {@link org.hidetake.groovy.ssh.operation.CommandSettings#inputStream},
 * {@link org.hidetake.groovy.ssh.operation.ShellSettings#inputStream},
 * {@link org.hidetake.groovy.ssh.session.execution.SudoSettings#inputStream}.
 *
 * @author Hidetake Iwata
 */
class InputStreamValue {

    private final value

    def InputStreamValue(def value1) {
        if (value == null ||
            value instanceof InputStream ||
            value instanceof byte[] ||
            value instanceof String ||
            value instanceof File) {
            value = value1
        } else {
            throw new IllegalArgumentException("inputStream must be InputStream, byte[], String or File: $value1")
        }
    }

    boolean asBoolean() {
        value != null
    }

    InputStreamValue rightShift(OutputStream stream) {
        if (value instanceof InputStream) {
            stream << value
        } else if (value instanceof byte[]) {
            stream << value
        } else if (value instanceof String) {
            stream << value
        } else if (value instanceof File) {
            value.withInputStream {
                stream << it
            }
        }
        this
    }

}
