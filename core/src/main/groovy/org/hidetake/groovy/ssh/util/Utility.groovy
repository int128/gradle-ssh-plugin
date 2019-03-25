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
    static <T> T callWithDelegate(Closure<T> closure, delegate, ... arguments) {
        def cloned = closure.clone() as Closure<T>
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned.delegate = delegate
        cloned.call(*arguments)
    }

    /**
     * Curry a method with self for recursive.
     *
     * @param closure
     * @return curried closure
     */
    static <T> Closure<T> currySelf(Closure<T> closure) {
        def curried
        curried = closure.curry {
            closure.call(curried)
        }
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
                ManagedBlocking.sleep(retryWaitSec * 1000L)
                retry(retryCount - 1, retryWaitSec, closure)
            }
        } else {
            closure()
        }
    }
}
