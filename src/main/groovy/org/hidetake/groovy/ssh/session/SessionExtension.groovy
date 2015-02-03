package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.operation.SftpOperations

/**
 * A base trait of session extensions.
 * Session extensions must apply this trait.
 *
 * @author Hidetake Iwata
 */
trait SessionExtension {
    /**
     * Returns remote host for the current session.
     *
     * @return the remote host
     */
    abstract Remote getRemote()

    /**
     * Perform SFTP operations.
     *
     * @param closure closure for {@link org.hidetake.groovy.ssh.operation.SftpOperations}
     */
    abstract void sftp(@DelegatesTo(SftpOperations) Closure closure)

    /**
     * Return the current {@link Operations}.
     * Only for DSL extensions, do not use from the script.
     */
    abstract Operations getOperations()

    /**
     * Return the current {@link OperationSettings}.
     * Only for DSL extensions, do not use from the script.
     */
    abstract OperationSettings getOperationSettings()
}
