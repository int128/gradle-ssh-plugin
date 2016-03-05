package org.hidetake.groovy.ssh.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.ConnectionManager
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.operation.DryRunOperations

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An executor of session {@link Plan}s.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Executor {
    /**
     * Execute {@link Plan}s.
     *
     * @param compositeSettings
     * @param plans
     * @return results of each plan
     */
    def <T> List<T> execute(CompositeSettings compositeSettings, List<Plan<T>> plans) {
        if (compositeSettings.dryRun) {
            dryRun(compositeSettings, plans)
        } else {
            wetRun(compositeSettings, plans)
        }
    }

    private <T> List<T> dryRun(CompositeSettings compositeSettings, List<Plan<T>> plans) {
        log.debug("Running ${plans.size()} session(s) as dry-run")
        plans.collect { plan ->
            def operations = new DryRunOperations(plan.remote)
            callWithDelegate(plan.closure, SessionHandler.create(operations, compositeSettings.operationSettings))
        }
    }

    private <T> List<T> wetRun(CompositeSettings compositeSettings, List<Plan<T>> plans) {
        log.debug("Running ${plans.size()} session(s)")
        def manager = new ConnectionManager(compositeSettings.connectionSettings)
        try {
            plans.collect { plan ->
                def connection = manager.connect(plan.remote)
                def operations = new DefaultOperations(connection)
                callWithDelegate(plan.closure, SessionHandler.create(operations, compositeSettings.operationSettings))
            }
        } finally {
            log.debug('Waiting for pending sessions')
            manager.waitAndClose()
            log.debug('Completed all sessions')
        }
    }
}
