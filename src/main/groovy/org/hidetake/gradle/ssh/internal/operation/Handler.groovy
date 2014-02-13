package org.hidetake.gradle.ssh.internal.operation

import org.hidetake.gradle.ssh.api.CommandContext
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.ShellSettings

/**
 *
 *
 * @see org.hidetake.gradle.ssh.api.Operation
 * @author hidetake.org
 */
interface Handler {
    void shell(ShellSettings settings, Closure interactions)

    String execute(ExecutionSettings settings, String command, Closure interactions)

    String executeSudo(ExecutionSettings settings, String command)

    CommandContext executeBackground(ExecutionSettings settings, String command)

    void get(String remote, String local)

    void put(String local, String remote)
}
