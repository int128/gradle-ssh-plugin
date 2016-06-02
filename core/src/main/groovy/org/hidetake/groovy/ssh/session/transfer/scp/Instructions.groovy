package org.hidetake.groovy.ssh.session.transfer.scp

import groovy.transform.TupleConstructor

class Instructions {

    @TupleConstructor
    @SuppressWarnings("GrFinalVariableAccess")
    static class EnterDirectory {
        final String name
    }

    @TupleConstructor
    @SuppressWarnings("GrFinalVariableAccess")
    static class Content {
        final String name
        final byte[] bytes
    }

    @Singleton
    static class LeaveDirectory {
    }

    final String remoteBase

    final boolean recursive

    /**
     * A list of instructions for the SCP command.
     * This should contain {@link Content}, {@link EnterDirectory}, {@link File} or {@link LeaveDirectory}.
     */
    private final instructions = []

    /**
     * Constructor for batch operation.
     * @param localFiles
     * @return
     */
    def Instructions(Iterable<File> localFiles, String remoteBase1) {
        recursive = true
        remoteBase = remoteBase1
        add(localFiles)
    }

    private add(Iterable<File> localFiles) {
        localFiles.findAll { !it.directory }.each { localFile ->
            instructions.add(localFile)
        }
        localFiles.findAll { it.directory }.each { localDir ->
            instructions.add(new EnterDirectory(localDir.name))
            add(localDir.listFiles().toList())
            instructions.add(LeaveDirectory.instance)
        }
    }

    /**
     * Constructor for single operation.
     * @param file
     * @return
     */
    def Instructions(byte[] bytes, String remotePath) {
        recursive = false
        def m = remotePath =~ '(.*/)(.+?)'
        if (m.matches()) {
            def remoteFilename = m.group(2)
            remoteBase = m.group(1)
            instructions.add(new Content(remoteFilename, bytes))
        } else {
            throw new IllegalArgumentException("Remote path must be an absolute path: $remotePath")
        }
    }

    Iterator iterator() {
        instructions.iterator()
    }

}
