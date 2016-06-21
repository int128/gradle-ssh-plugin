package org.hidetake.groovy.ssh.session.transfer.put

/**
 * Represents a collection of SCP PUT instructions
 * such as {@link File}, {@link StreamContent}, {@link EnterDirectory} or {@link LeaveDirectory}.
 *
 * @author Hidetake Iwata
 */
class Instructions implements Iterable {

    final boolean recursive
    final String base
    private final Collection all

    def Instructions(boolean recursive1, String base1, Collection all1) {
        recursive = recursive1
        base = base1
        all = all1
    }

    @Override
    Iterator iterator() {
        all.iterator()
    }

    /**
     * Create {@link Instructions} for a file or directory.
     *
     * @param localPath a file or directory
     * @param remoteBase base path of remote
     * @return an instance of {@link Instructions}
     */
    static Instructions forFile(File localPath, String remoteBase) {
        new Instructions(true, remoteBase, forFileRecursive(localPath))
    }

    /**
     * Create {@link Instructions} for a collection of files or directories
     *
     * @param localFiles a collection of files or directories
     * @param remoteBase base path of remote
     * @return an instance of {@link Instructions}
     */
    static Instructions forFiles(Iterable<File> localFiles, String remoteBase) {
        new Instructions(true, remoteBase, localFiles.collect { localFile -> forFileRecursive(localFile)}.flatten())
    }

    private static forFileRecursive(File path, List all = []) {
        if (path.directory) {
            all.add(new EnterDirectory(path.name))
            path.eachFile { file -> forFileRecursive(file, all) }
            path.eachDir { dir -> forFileRecursive(dir, all) }
            all.add(LeaveDirectory.instance)
        } else {
            all.add(path)
        }
        all
    }

    /**
     * Create {@link Instructions} for stream content.
     *
     * @param localPath a file or directory
     * @param remoteBase base path of remote
     * @return an instance of {@link Instructions}
     */
    static Instructions forStreamContent(InputStream stream, String path) {
        def m = path =~ '(.*/)(.+?)'
        if (m.matches()) {
            def filename = m.group(2)
            def dirname = m.group(1)
            new Instructions(false, dirname, [new StreamContent(filename, stream)])
        } else {
            throw new IllegalArgumentException("Remote path must be an absolute path: $path")
        }
    }

}
