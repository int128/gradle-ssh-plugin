package org.hidetake.groovy.ssh.session.transfer.get

import groovy.util.logging.Slf4j

@Slf4j
class FileReceiver implements WritableReceiver {

    final File destination

    @Lazy
    private recreateAtFirst = {
        if (destination.exists()) {
            destination.delete()
        }
        destination.createNewFile()
        ({})
    }()

    def FileReceiver(File destination1) {
        destination = destination1
        assert !destination.directory
    }

    @Override
    void write(byte[] bytes) {
        recreateAtFirst()
        log.trace("Writing $bytes.length bytes into file: $destination")
        destination.append(bytes)
    }

}
