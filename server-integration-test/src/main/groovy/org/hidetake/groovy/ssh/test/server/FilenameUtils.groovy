package org.hidetake.groovy.ssh.test.server

class FilenameUtils {

    static String toUnixSeparator(String path){
        isWindows() ? path.replaceAll('\\\\','/') : path
    }

    static boolean isWindows(){
        System.getProperty('os.name').toLowerCase().contains('windows')
    }

}
