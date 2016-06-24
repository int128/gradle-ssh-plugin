package org.hidetake.groovy.ssh.test.server

class FilenameUtils {

    /**
     * Convert Windows path to Unix path for SFTP subsystem of Apache SSHD server.
     * This method does nothing on Unix platform.
     *
     * @param path
     * @return
     */
    static String toUnixPath(String path) {
        if (File.separator == '/') {
            path
        } else {
            path.replace(File.separatorChar, '/' as char).replaceFirst(~/(\w):/, '/$1')
        }
    }

}
