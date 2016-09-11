package org.hidetake.groovy.ssh.session

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.ConnectionManager
import org.hidetake.groovy.ssh.connection.JSchLogger
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.GlobalSettings
import org.hidetake.groovy.ssh.core.settings.PerServiceSettings
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.operation.DryRunOperations

import java.util.concurrent.Callable

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A session with global and per-service settings.
 *
 * @author Hidetake Iwata
 */
@Slf4j
@EqualsAndHashCode
class SessionTask<T> implements Callable<T> {

    final Session<T> session
    final GlobalSettings globalSettings
    final PerServiceSettings perServiceSettings

    def SessionTask(Session<T> session1, GlobalSettings globalSettings1, PerServiceSettings perServiceSettings1) {
        session = session1
        globalSettings = globalSettings1
        perServiceSettings = perServiceSettings1
    }

    @Override
    def T call() {
        log.debug("Using per-remote settings: ${new CompositeSettings.With(session.remote)}")
        def settings = new SessionSettings.With(
            CompositeSettings.With.DEFAULT,
            globalSettings,
            perServiceSettings,
            session.remote)
        JSchLogger.enabledInCurrentThread = settings.jschLog
        if (settings.dryRun) {
            dryRun()
        } else {
            wetRun()
        }
    }

    private T dryRun() {
        def operations = new DryRunOperations(session.remote)
        def sessionHandler = SessionHandler.create(operations, globalSettings, perServiceSettings)
        callWithDelegate(session.closure, sessionHandler)
    }

    private T wetRun() {
        def manager = new ConnectionManager(globalSettings, perServiceSettings)
        try {
            def connection = manager.connect(session.remote)
            def operations = new DefaultOperations(connection)
            def sessionHandler = SessionHandler.create(operations, globalSettings, perServiceSettings)
            callWithDelegate(session.closure, sessionHandler)
        } finally {
            manager.close()
        }
    }

}
