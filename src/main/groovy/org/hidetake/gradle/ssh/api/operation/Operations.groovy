package org.hidetake.gradle.ssh.api.operation

import org.hidetake.gradle.ssh.api.Remote
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
         * @return an instance for wet run
         */
        Operations create(Connection connection)

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

    void shell(OperationSettings settings)

    String execute(OperationSettings settings, String command, Closure callback)

    void executeBackground(OperationSettings settings, String command, Closure callback)

    /**
     * Perform SFTP operations.
     *
     * @param closure closure for {@link SftpHandler}
     */
    void sftp(Closure closure)
}
