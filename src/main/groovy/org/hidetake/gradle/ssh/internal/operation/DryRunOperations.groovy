package org.hidetake.gradle.ssh.internal.operation

import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings

/**
 * Dry-run implementation of {@link org.hidetake.gradle.ssh.api.operation.Operations}.
 *
 * @author hidetake.org
 */
@TupleConstructor
class DryRunOperations implements Operations {
    final Remote remote

    @Override
    void shell(ShellSettings settings, Closure closure) {
    }

    @Override
    String execute(ExecutionSettings settings, String command, Closure closure) {
        ''
    }

    @Override
    void executeBackground(ExecutionSettings settings, String command) {
    }

    @Override
    void sftp(Closure closure) {
    }
}
