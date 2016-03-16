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
    final CompositeSettings settings

    def Executor(CompositeSettings settings1) {
        settings = settings1
        assert settings
    }

    /**
     * Execute {@link Plan}s.
     *
     * @param globalSettings
     * @param plans
     * @return results of each plan
     */
    def <T> List<T> execute(List<Plan<T>> plans) {
        if (settings.dryRun) {
            dryRun(plans)
        } else {
            wetRun(plans)
        }
    }

    private <T> List<T> dryRun(List<Plan<T>> plans) {
        log.debug("Running ${plans.size()} session(s) as dry-run")
        plans.collect { plan ->
            def operations = new DryRunOperations(plan.remote)
            callWithDelegate(plan.closure, SessionHandler.create(operations, settings))
        }
    }

    private <T> List<T> wetRun(List<Plan<T>> plans) {
        log.debug("Running ${plans.size()} session(s)")
        def manager = new ConnectionManager(settings.connectionSettings)
        try {
            plans.collect { plan ->
                def connection = manager.connect(plan.remote)
                def operations = new DefaultOperations(connection)
                callWithDelegate(plan.closure, SessionHandler.create(operations, settings))
            }
        } finally {
            log.debug('Waiting for pending sessions')
            manager.waitAndClose()
            log.debug('Completed all sessions')
        }
    }
}
