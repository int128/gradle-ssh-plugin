package org.hidetake.gradle.ssh.api.operation

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.ssh.Connection
import org.hidetake.gradle.ssh.registry.Registry

/**
 * Interface of operations.
 *
 * @author hidetake.org
 */
interface Operations {
    /**
     * A factory of {@link Operations}.
     */
    interface Factory {
        /**
         * Create an instance.
         *
         * @param connection
         * @param sshSettings
         * @return an instance for wet run
         */
        Operations create(Connection connection, SshSettings sshSettings)

        /**
         * Create an instance for dry run.
         *
         * @param remote
         * @return an instance for dry run
         */
        Operations create(Remote remote)
    }

    final factory = Registry.instance[Factory]

    Remote getRemote()

    void shell(ShellSettings settings, Closure closure)

    String execute(ExecutionSettings settings, String command, Closure closure)

    void executeBackground(ExecutionSettings settings, String command)

    void get(String remote, String local)

    void put(String local, String remote)
}
