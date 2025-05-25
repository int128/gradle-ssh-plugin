package org.hidetake.groovy.ssh.connection

class AddHostKey {

    final File knownHostsFile

    def AddHostKey(File knownHostsFile1) {
        knownHostsFile = knownHostsFile1
        assert knownHostsFile
    }

    @Override
    String toString() {
        "addHostKey($knownHostsFile)"
    }

}
