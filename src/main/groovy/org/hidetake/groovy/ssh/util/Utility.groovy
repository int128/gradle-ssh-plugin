package org.hidetake.groovy.ssh.util

class Utility {
    /**
     * Find not null item from arguments in order.
     * They should be same type.
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
