package org.hidetake.groovy.ssh.operation

class DryRunOperation implements Operation {
    @Override
    int startSync() {
        0
    }

    @Override
    void startAsync(Closure closure) {
        closure.call(0)
    }

    @Override
    void onEachLineOfStandardOutput(Closure closure) {
    }
}
