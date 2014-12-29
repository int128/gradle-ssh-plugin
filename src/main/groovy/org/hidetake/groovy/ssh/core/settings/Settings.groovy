package org.hidetake.groovy.ssh.core.settings

/**
 * Represents a settings class.
 *
 * @param < T > implemented type
 */
abstract class Settings<T> {
    /**
     * Compute a merged settings.
     *
     * @param right
     * @return
     */
    abstract T plus(T right)

    /**
     * Find not null item from arguments in order.
     *
     * @param first
     * @param second
     * @return first if not null, second otherwise
     */
    static <E> E findNotNull(E first, E second) {
        if (first != null) {
            first
        } else {
            second
        }
    }
}
