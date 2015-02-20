package org.hidetake.groovy.ssh.util

import com.jcraft.jsch.JSchException
import groovy.util.logging.Slf4j

/**
 * Provides utility methods.
 *
 * @author Hidetake Iwata
 */
@Slf4j
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

    /**
     * Execute the closure with retrying.
     * This method catches only {@link com.jcraft.jsch.JSchException}s.
     *
     * @param retryCount
     * @param retryWaitSec
     * @param closure
     */
    static <T> T retry(int retryCount, int retryWaitSec, Closure<T> closure) {
        assert closure != null, 'closure should be set'
        if (retryCount > 0) {
            try {
                closure()
            } catch (JSchException e) {
                log.warn("Retrying: ${e.getClass().name}: ${e.localizedMessage}")
                sleep(retryWaitSec * 1000L)
                retry(retryCount - 1, retryWaitSec, closure)
            }
        } else {
            closure()
        }
    }
}
