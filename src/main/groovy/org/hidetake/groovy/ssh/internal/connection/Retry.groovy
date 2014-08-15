package org.hidetake.groovy.ssh.internal.connection

import com.jcraft.jsch.JSchException
import groovy.util.logging.Slf4j

@Slf4j
class Retry {
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
                log.warn("Retrying connection: ${e.getClass().name}: ${e.localizedMessage}")
                sleep(retryWaitSec * 1000L)
                retry(retryCount - 1, retryWaitSec, closure)
            }
        } else {
            closure()
        }
    }
}
