package org.hidetake.gradle.ssh.test

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSpec

class TestDataHelper {
    static SshSpec createSpec() {
        new SshSpec().with {
            sessionSpecs.addAll([new SessionSpec(createRemote(), {-> println "whatever" })])
            knownHosts = new File("known_hosts")
            dryRun = false
            retryCount = 1
            retryWaitSec = 1
            outputLogLevel = LogLevel.LIFECYCLE
            errorLogLevel = LogLevel.LIFECYCLE

            it
        }
    }

    static Remote createRemote() {
        createRemote([:])
    }

    static Remote createRemote(Map args) {
        def keys = args.keySet()

        new Remote(keys.contains("name") ? args.name : "myRemote").with {
            user = keys.contains("user") ? args.user : "myUser"
            host = keys.contains("host") ? args.host : "myHost"
            it
        }
    }

}

