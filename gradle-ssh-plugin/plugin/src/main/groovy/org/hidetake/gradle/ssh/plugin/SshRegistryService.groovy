package org.hidetake.gradle.ssh.plugin

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote

abstract class SshRegistryService implements BuildService<BuildServiceParameters.None> {
    private final Map<String, Proxy> proxies = [:]
    private final Map<String, Remote> remotes = [:]

    void registerProxy(Proxy proxy) {
        proxies[proxy.name] = proxy
    }

    void registerRemote(Remote remote) {
        remotes[remote.name] = remote
    }

    Collection<Proxy> allProxies() {
        proxies.values()
    }

    Collection<Remote> allRemotes() {
        remotes.values()
    }
}
