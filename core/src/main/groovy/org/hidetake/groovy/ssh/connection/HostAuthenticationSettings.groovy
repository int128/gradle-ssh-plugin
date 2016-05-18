package org.hidetake.groovy.ssh.connection

trait HostAuthenticationSettings {

    /**
     * Known hosts file.
     * This can be a {@link File}, {@link List<File>} or {@link #allowAnyHosts}.
     */
    def knownHosts

    /**
     * Represents that strict host key checking is turned off and any host is allowed.
     * @see #knownHosts
     */
    final allowAnyHosts = AllowAnyHosts.instance

    /**
     * Hides constant from result of {@link #toString()}.
     */
    def toString__allowAnyHosts() {}

}
