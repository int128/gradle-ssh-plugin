package org.hidetake.groovy.ssh.internal.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.internal.connection.ConnectionService
import org.hidetake.groovy.ssh.internal.connection.DefaultConnectionService
import org.hidetake.groovy.ssh.internal.operation.DefaultOperations
import org.hidetake.groovy.ssh.internal.operation.DryRunOperations

import static org.hidetake.groovy.ssh.internal.util.ClosureUtil.callWithDelegate

@Slf4j
class DefaultSessionExecutor implements SessionExecutor {
    private final ConnectionService connectionService

    def DefaultSessionExecutor(ConnectionService connectionService1 = new DefaultConnectionService()) {
        connectionService = connectionService1
    }

    @Override
    def <T> List<T> execute(CompositeSettings compositeSettings, List<Plan<T>> plans) {
        if (compositeSettings.dryRun) {
            plans.collect { plan ->
                def operations = new DryRunOperations(plan.remote)
                def handler = new DefaultSessionHandler(operations, compositeSettings.operationSettings)
                log.debug("Mixin extensions: ${compositeSettings.extensions}")
                handler.metaClass.mixin(compositeSettings.extensions)
                callWithDelegate(plan.closure, handler)
            }
        } else {
            connectionService.withManager(compositeSettings.connectionSettings) { manager ->
                plans.collect { plan ->
                    def connection = manager.connect(plan.remote)
                    def operations = new DefaultOperations(connection)
                    def handler = new DefaultSessionHandler(operations, compositeSettings.operationSettings)
                    log.debug("Mixin extensions: ${compositeSettings.extensions}")
                    handler.metaClass.mixin(compositeSettings.extensions)
                    callWithDelegate(plan.closure, handler)
                }
            }
        }
    }
}
