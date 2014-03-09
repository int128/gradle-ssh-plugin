package org.hidetake.gradle.ssh.api

import groovy.transform.TupleConstructor

/**
 * Specification of a session.
 *
 * @author hidetake.org
 *
 */
@TupleConstructor
class SessionSpec {
    /**
     * Remote.
     */
    final Remote remote

    /**
     * Closure for {@link org.hidetake.gradle.ssh.api.session.SessionHandler}.
     */
    final Closure operationClosure
}
