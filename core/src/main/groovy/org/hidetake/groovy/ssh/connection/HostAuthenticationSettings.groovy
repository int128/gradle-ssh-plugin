package org.hidetake.groovy.ssh.connection

trait HostAuthenticationSettings {

    /**
     * Known hosts file.
     * If {@link #allowAnyHosts} is set, strict host key checking is turned off.
     */
    File knownHosts

    /**
     * Represents that strict host key checking is turned off and any host is allowed.
     * @see #knownHosts
     */
    final File allowAnyHosts = ConnectionSettings.Constants.allowAnyHosts

    /**
     * Hides constant from result of {@link #toString()}.
     */
    def toString__allowAnyHosts() {}

}
