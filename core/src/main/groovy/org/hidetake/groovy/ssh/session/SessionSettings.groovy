package org.hidetake.groovy.ssh.session

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.SettingsHelper
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for {@link org.hidetake.groovy.ssh.session.SessionHandler}s.
 *
 * @author Hidetake Iwata
 */
trait SessionSettings {
    /**
     * Dry-run flag.
     * If <code>true</code>, performs no action.
     */
    Boolean dryRun

    /**
     * JSch logging flag.
     */
    Boolean jschLog

    /**
     * Extensions for {@link org.hidetake.groovy.ssh.session.SessionHandler}.
     */
    List extensions = []

    /**
     * Do not show if it is empty or null.
     */
    def toString__extensions() { extensions ? extensions : null }

    def plus__extensions(SessionSettings prior) {
        assert prior.extensions instanceof List
        extensions + prior.extensions
    }


    @EqualsAndHashCode
    static class With implements SessionSettings, ToStringProperties {
        def With() {}
        def With(SessionSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final SessionSettings DEFAULT = new SessionSettings.With(
                dryRun: false,
                jschLog: false,
                extensions: [],
        )
    }
}
