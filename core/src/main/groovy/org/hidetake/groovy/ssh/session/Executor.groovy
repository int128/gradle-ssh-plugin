package org.hidetake.groovy.ssh.session

import com.jcraft.jsch.JSch
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.ConnectionManager
import org.hidetake.groovy.ssh.connection.JSchLogger
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.GlobalSettings
import org.hidetake.groovy.ssh.core.settings.PerServiceSettings
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
    private final GlobalSettings globalSettings
    private final PerServiceSettings perServiceSettings

    def Executor(GlobalSettings globalSettings1, PerServiceSettings perServiceSettings1) {
        globalSettings = globalSettings1
        perServiceSettings = perServiceSettings1
        assert globalSettings
        assert perServiceSettings
    }

    /**
     * Execute {@link Plan}s.
     *
     * @param plans
     * @return results of each plan
     */
    def <T> List<T> execute(List<Plan<T>> plans) {
        log.debug("Using default settings: $CompositeSettings.With.DEFAULT")
        log.debug("Using global settings: $globalSettings")
        log.debug("Using per-service settings: $perServiceSettings")
        def mergedSettings = new CompositeSettings.With(CompositeSettings.With.DEFAULT, globalSettings, perServiceSettings)

        // not thread safe
        JSch.logger = mergedSettings.jschLog ? JSchLogger.instance : null

        if (mergedSettings.dryRun) {
            dryRun(plans, mergedSettings)
        } else {
            wetRun(plans, mergedSettings)
        }
    }

    private static <T> List<T> dryRun(List<Plan<T>> plans, CompositeSettings mergedSettings) {
        plans.collect { plan ->
            log.debug("Using per-remote settings: ${new CompositeSettings.With(plan.remote)}")
            def operations = new DryRunOperations(plan.remote)
            def sessionHandler = SessionHandler.create(operations, new CompositeSettings.With(mergedSettings, plan.remote))
            callWithDelegate(plan.closure, sessionHandler)
        }
    }

    private static <T> List<T> wetRun(List<Plan<T>> plans, CompositeSettings mergedSettings) {
        def manager = new ConnectionManager(mergedSettings)
        try {
            plans.collect { plan ->
                log.debug("Using per-remote settings: ${new CompositeSettings.With(plan.remote)}")
                def connection = manager.connect(plan.remote)
                def operations = new DefaultOperations(connection)
                def sessionHandler = SessionHandler.create(operations, new CompositeSettings.With(mergedSettings, plan.remote))
                callWithDelegate(plan.closure, sessionHandler)
            }
        } finally {
            log.debug('Waiting for pending sessions')
            manager.waitAndClose()
            log.debug('Completed all sessions')
        }
    }
}
