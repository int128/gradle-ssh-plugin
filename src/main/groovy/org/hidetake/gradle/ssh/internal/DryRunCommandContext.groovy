package org.hidetake.gradle.ssh.internal

import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.CommandContext

/**
 * Null implementation of {@link CommandContext} for dry-run.
 *
 * @author hidetake.org
 */
@Slf4j
class DryRunCommandContext implements CommandContext {
    @Override
    void interaction(Closure closure) {
        log.info("Registering stream interaction (${closure.toString()})")
    }
}
