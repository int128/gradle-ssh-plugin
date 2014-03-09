package org.hidetake.gradle.ssh.api.operation

import org.hidetake.gradle.ssh.api.Remote

/**
 * Interface of operations.
 *
 * @author hidetake.org
 */
interface Operations {
    Remote getRemote()

    void shell(ShellSettings settings, Closure closure)

    String execute(ExecutionSettings settings, String command, Closure closure)

    String executeSudo(ExecutionSettings settings, String command)

    void executeBackground(ExecutionSettings settings, String command)

    void get(String remote, String local)

    void put(String local, String remote)
}
