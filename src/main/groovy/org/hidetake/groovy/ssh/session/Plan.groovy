package org.hidetake.groovy.ssh.session

import groovy.transform.Canonical
import org.hidetake.groovy.ssh.core.Remote

/**
 * An execution plan of SSH session.
 *
 * @param < T >
 * @author Hidetake Iwata
 */
@Canonical
class Plan<T> {
    /**
     * Remote host to be connected.
     */
    final Remote remote

    /**
     * Operations.
     */
    final Closure<T> closure
}
