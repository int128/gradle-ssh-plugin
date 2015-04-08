package org.hidetake.groovy.ssh.util

class FilenameUtils {

    static String toUnixSeparator(String path){
        isWindows()?path.replaceAll('\\\\','/'):path
    }

    static boolean isWindows(){
        System.properties['os.name'].toLowerCase().contains('windows')
    }
}
