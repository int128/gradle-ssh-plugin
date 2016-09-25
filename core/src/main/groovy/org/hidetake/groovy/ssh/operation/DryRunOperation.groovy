package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.interaction.InteractionHandler

class DryRunOperation implements Operation {
    @Override
    int execute() {
        0
    }

    @Override
    void addInteraction(@DelegatesTo(InteractionHandler) Closure closure) {
    }
}
