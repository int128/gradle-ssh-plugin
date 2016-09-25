package org.hidetake.groovy.ssh.session

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

/**
 * A session.
 *
 * @author Hidetake Iwata
 */
@Slf4j
@EqualsAndHashCode
class Session<T> {

    final Remote remote
    final Closure<T> closure

    def Session(Remote remote1, Closure<T> closure1) {
        remote = remote1
        closure = closure1
    }

}
