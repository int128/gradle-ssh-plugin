package org.hidetake.groovy.ssh.session

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.PlusProperties
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for {@link org.hidetake.groovy.ssh.session.SessionHandler}s.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
class SessionSettings implements PlusProperties<SessionSettings>, ToStringProperties {
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

    final plus__extensions(right) {
        assert right.extensions instanceof List
        extensions + right.extensions
    }

    static final DEFAULT = new SessionSettings(
            dryRun: false,
            extensions: []
    )
}
