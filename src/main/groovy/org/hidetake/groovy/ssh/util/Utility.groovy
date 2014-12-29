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

    static <T> T callWithDelegate(Closure<T> closure, Object delegate) {
        def cloned = closure.clone() as Closure<T>
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned.delegate = delegate
        cloned.call()
    }
}
