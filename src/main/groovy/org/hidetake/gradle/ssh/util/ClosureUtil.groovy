package org.hidetake.gradle.ssh.util

/**
 * An utility class of the closure.
 *
 * @author Hidetake Iwata
 */
class ClosureUtil {
    static <T> T callWithDelegate(Closure<T> closure, Object delegate) {
        def cloned = closure.clone() as Closure<T>
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned.delegate = delegate
        cloned.call()
    }
}
