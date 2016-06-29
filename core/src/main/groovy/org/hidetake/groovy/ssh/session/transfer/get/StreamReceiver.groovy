package org.hidetake.groovy.ssh.session.transfer.get

import groovy.util.logging.Slf4j

@Slf4j
class StreamReceiver implements WritableReceiver {

    final OutputStream stream

    def StreamReceiver(OutputStream stream1) {
        stream = stream1
    }

    @Override
    void write(byte[] bytes) {
        log.trace("Writing $bytes.length bytes into the stream")
        stream.write(bytes)
    }

}
