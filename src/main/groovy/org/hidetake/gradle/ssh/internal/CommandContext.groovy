package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.CommandPromise

@TupleConstructor
class CommandContext implements CommandPromise {
    final Channel channel
    final LoggingOutputStream standardOutput
    final LoggingOutputStream standardError
}
