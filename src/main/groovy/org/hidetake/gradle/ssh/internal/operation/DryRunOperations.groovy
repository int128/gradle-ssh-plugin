package org.hidetake.gradle.ssh.internal.operation

import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.operation.Operations

/**
 * Dry-run implementation of {@link org.hidetake.gradle.ssh.api.operation.Operations}.
 *
 * @author hidetake.org
 */
@TupleConstructor
class DryRunOperations implements Operations {
    final Remote remote

    @Override
    void shell(OperationSettings settings) {
    }

    @Override
    String execute(OperationSettings settings, String command, Closure callback) {
        callback?.call('')
        ''
    }

    @Override
    void executeBackground(OperationSettings settings, String command, Closure callback) {
        callback?.call('')
    }

    @Override
    void sftp(Closure closure) {
    }
}
