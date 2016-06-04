package org.hidetake.groovy.ssh.session.transfer.scp

class FileReceiver implements Receiver {

    private final OutputStream stream

    final boolean recursive = false

    def FileReceiver(OutputStream stream1) {
        stream = stream1
    }

    @Override
    def foundFile(String name, long size, int mode) {
    }

    @Override
    void receiveContent(byte[] bytes, def context) {
        stream.write(bytes)
    }

    @Override
    void enterDirectory(String name, int mode) {
        throw new IllegalStateException("Expected single file but found directory: $name")
    }

    @Override
    void leaveDirectory() {
        throw new IllegalStateException("Expected single file but found directory")
    }

}
