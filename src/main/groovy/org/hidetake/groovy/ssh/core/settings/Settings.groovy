package org.hidetake.groovy.ssh.core.settings

/**
 * Represents a settings class.
 *
 * @param < T > implemented type
 */
interface Settings<T> {
    /**
     * Compute a merged settings.
     *
     * @param right
     * @return
     */
    T plus(T right)
}
