package org.hidetake.groovy.ssh.session.transfer.scp

import groovy.util.logging.Slf4j

@Slf4j
class DirectoryReceiver implements Receiver<File> {

    private final localDirStack = new ArrayDeque<File>()

    final boolean recursive = true

    def DirectoryReceiver(File localDir) {
        localDirStack.add(localDir)
    }

    @Override
    File foundFile(String name, long size, int mode) {
        def localFile = new File(currentLocalDir, name)
        localFile.delete()
        localFile
    }

    @Override
    void receiveContent(byte[] bytes, File context) {
        context.append(bytes)
        log.trace("Wrote $bytes.length bytes into $context.path")
    }

    @Override
    void enterDirectory(String name, int mode) {
        localDirStack.push(new File(currentLocalDir, name))
        currentLocalDir.mkdir()
        log.trace("Created local directory: $currentLocalDir.path")
    }

    @Override
    void leaveDirectory() {
        localDirStack.pop()
    }

    private getCurrentLocalDir() {
        localDirStack.peek()
    }

}
