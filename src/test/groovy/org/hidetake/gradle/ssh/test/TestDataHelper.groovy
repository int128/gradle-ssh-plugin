package org.hidetake.gradle.ssh.test

import org.hidetake.gradle.ssh.api.Remote

class TestDataHelper {
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

