package org.hidetake.groovy.ssh.session.transfer.get

import groovy.util.logging.Slf4j

@Slf4j
class RecursiveReceiver {

    final File destination

    private final Closure<Boolean> filter
    private final List<File> directoryStack

    def RecursiveReceiver(File destination1, Closure<Boolean> filter1) {
        destination = destination1
        filter = filter1
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
        def directory = directoryStack.last()
        def file = new File(directory, name)
        if (!filter || filter.call(file)) {
            if (!directory.exists()) {
                directory.mkdirs()
            } else if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            file
        } else {
            null
        }
    }

    /**
     * Called when it entered into the remote directory.
     *
     * @param name
     */
    void enterDirectory(String name) {
        def directory = new File(directoryStack.last(), name)
        directoryStack.push(directory)
        if (!filter) {
            directory.mkdirs()
        }
    }

    /**
     * Called when it left from the remote directory.
     */
    void leaveDirectory() {
        directoryStack.pop()
    }

}
