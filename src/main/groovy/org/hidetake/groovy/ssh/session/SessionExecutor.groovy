package org.hidetake.groovy.ssh.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.ConnectionSettings
import org.hidetake.groovy.ssh.connection.ConnectionManager
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.operation.DryRunOperations

import static org.hidetake.groovy.ssh.util.ClosureUtil.callWithDelegate

/**
 * An executor of session {@link Plan}s.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SessionExecutor {
    /**
     * Execute {@link Plan}s.
     *
     * @param compositeSettings
     * @param plans
     * @return results of each plan
     */
    def <T> List<T> execute(CompositeSettings compositeSettings, List<Plan<T>> plans) {
        if (compositeSettings.dryRun) {
            plans.collect { plan ->
                def operations = new DryRunOperations(plan.remote)
                def handler = new SessionHandler(operations, compositeSettings.operationSettings)
                log.debug("Mixin extensions: ${compositeSettings.extensions}")
                handler.metaClass.mixin(compositeSettings.extensions)
                callWithDelegate(plan.closure, handler)
            }
        } else {
            withConnectionManager(compositeSettings.connectionSettings) { ConnectionManager manager ->
                plans.collect { plan ->
                    def connection = manager.connect(plan.remote)
                    def operations = new DefaultOperations(connection)
                    def handler = new SessionHandler(operations, compositeSettings.operationSettings)
                    log.debug("Mixin extensions: ${compositeSettings.extensions}")
                    handler.metaClass.mixin(compositeSettings.extensions)
                    callWithDelegate(plan.closure, handler)
                }
            }
        }
    }

    private static <T> T withConnectionManager(ConnectionSettings connectionSettings, Closure<T> closure) {
        def connectionManager = new ConnectionManager(connectionSettings)
        try {
            closure.call(connectionManager)
        } finally {
            connectionManager.waitAndClose()
        }
    }
}
