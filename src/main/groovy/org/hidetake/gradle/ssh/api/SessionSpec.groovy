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
     * Closure for {@link OperationHandler}.
     */
    final Closure operationClosure
}
