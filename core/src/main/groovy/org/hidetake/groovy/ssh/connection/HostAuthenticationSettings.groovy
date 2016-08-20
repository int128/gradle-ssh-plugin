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
     * Represents that a host key is automatically appended to the known hosts file.
     * @param knownHostsFile
     * @return
     * @see #knownHosts
     */
    AddHostKey addHostKey(File knownHostsFile) {
        new AddHostKey(knownHostsFile)
    }

    /**
     * Hides constant from result of {@link #toString()}.
     */
    def toString__allowAnyHosts() {}

}
