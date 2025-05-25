package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
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
     * @return result of the closure
     */
    abstract def <T> T sftp(@DelegatesTo(SftpOperations) Closure<T> closure)

    /**
     * Return the current {@link Operations}.
     * Only for DSL extensions, do not use from the script.
     */
    abstract Operations getOperations()

    /**
     * Return the settings with default, global, per-service and per-remote.
     * Only for DSL extensions, do not use from the script.
     */
    abstract CompositeSettings getMergedSettings()
}
