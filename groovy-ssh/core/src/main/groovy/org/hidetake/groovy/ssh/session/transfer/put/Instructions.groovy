package org.hidetake.groovy.ssh.session.transfer.put

import groovy.io.FileType

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

    private static Collection forFileRecursive(File path) {
        if (path.directory) {
            def children = []
            children.add(new EnterDirectory(path.name))
            path.eachFile(FileType.FILES) { file -> children.addAll(forFileRecursive(file)) }
            path.eachDir { dir -> children.addAll(forFileRecursive(dir)) }
            children.add(LeaveDirectory.instance)
            children
        } else {
            [path]
        }
    }

    /**
     * Create {@link Instructions} for filtered files.
     *
     * @param localPath a file or directory
     * @param remoteBase base path of remote
     * @param filter a closure called with a {@link File}
     * @return an instance of {@link Instructions}
     */
    static Instructions forFileWithFilter(File localPath, String remoteBase, Closure<Boolean> filter) {
        new Instructions(true, remoteBase, forFileWithFilterRecursive(localPath, filter))
    }

    private static Collection forFileWithFilterRecursive(File path, Closure<Boolean> filter) {
        if (path.directory) {
            def children = new ArrayDeque()
            path.eachFile(FileType.FILES) { file -> children.addAll(forFileWithFilterRecursive(file, filter)) }
            path.eachDir { dir -> children.addAll(forFileWithFilterRecursive(dir, filter)) }
            if (!children.empty) {
                children.addFirst(new EnterDirectory(path.name))
                children.addLast(LeaveDirectory.instance)
            }
            children
        } else {
            filter.call(path) ? [path] : []
        }
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
