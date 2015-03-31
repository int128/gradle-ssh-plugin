package org.hidetake.groovy.ssh.util

class FilenameUtils {

    static String toUnixSeparator(String path){
        path.replaceAll('\\\\','/')
    }
}
