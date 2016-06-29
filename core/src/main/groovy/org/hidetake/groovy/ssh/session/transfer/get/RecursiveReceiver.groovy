package org.hidetake.groovy.ssh.session.transfer.get

import groovy.util.logging.Slf4j

@Slf4j
class RecursiveReceiver {

    final File destination

    private final List<File> directoryStack

    def RecursiveReceiver(File destination1) {
        destination = destination1
        directoryStack = [destination]
        assert destination.directory
    }

    /**
     * Called when the remote file is found.
     *
     * @param name
     * @return file
     */
    File createFile(String name) {
        def child = new File(directoryStack.last(), name)
        log.trace("Recreating destination file: $child")
        if (child.file) {
            child.delete()
        }
        child.createNewFile()
        child
    }

    /**
     * Called when it entered into the remote directory.
     *
     * @param name
     */
    void enterDirectory(String name) {
        def child = new File(directoryStack.last(), name)
        child.mkdirs()
        directoryStack.push(child)
    }

    /**
     * Called when it left from the remote directory.
     */
    void leaveDirectory() {
        directoryStack.pop()
    }

}
