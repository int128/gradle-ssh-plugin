package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel

interface ChannelObservable {
    Channel getChannel()
}
