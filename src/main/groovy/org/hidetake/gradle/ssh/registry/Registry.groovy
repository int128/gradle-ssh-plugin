package org.hidetake.gradle.ssh.registry

import org.hidetake.gradle.ssh.api.task.Executor
import org.hidetake.gradle.ssh.internal.task.DefaultExecutor

@Singleton
class Registry extends AbstractRegistry {
    @Override
    void wire() {
        this[Executor] = DefaultExecutor.instance
    }
}
