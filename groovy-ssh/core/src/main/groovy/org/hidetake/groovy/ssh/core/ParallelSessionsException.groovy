package org.hidetake.groovy.ssh.core

import java.util.concurrent.ForkJoinTask

/**
 * An exception for parallel sessions.
 *
 * @author Hidetake Iwata
 */
class ParallelSessionsException extends Exception {
    final List<ForkJoinTask> tasks
    final List<Throwable> causes

    def ParallelSessionsException(String message, List<ForkJoinTask> tasks1) {
        super(message, firstCause(tasks1))
        tasks = tasks1
        causes = tasks*.exception.findAll()
    }

    private static firstCause(List<ForkJoinTask> tasks) {
        tasks.find { task -> task.completedAbnormally }.exception
    }
}
