package org.hidetake.groovy.ssh.session

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.Settings
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

/**
 * Settings for {@link org.hidetake.groovy.ssh.session.SessionHandler}s.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
class SessionSettings implements Settings<SessionSettings>, ToStringProperties {
    /**
     * Dry-run flag.
     * If <code>true</code>, performs no action.
     */
    Boolean dryRun

    /**
     * Extensions for {@link org.hidetake.groovy.ssh.session.SessionHandler}.
     */
    List extensions = []

    /**
     * Do not show if it is empty or null.
     */
    final toString__extensions() { extensions ? extensions : null }

    static final DEFAULT = new SessionSettings(
            dryRun: false,
            extensions: []
    )

    SessionSettings plus(SessionSettings right) {
        new SessionSettings(
                dryRun:         findNotNull(right.dryRun, dryRun),
                extensions:     extensions + right.extensions,
        )
    }
}
