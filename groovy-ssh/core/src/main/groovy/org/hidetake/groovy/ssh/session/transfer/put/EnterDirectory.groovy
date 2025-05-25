package org.hidetake.groovy.ssh.session.transfer.put

import groovy.transform.ToString

@ToString
class EnterDirectory {
    final String name

    def EnterDirectory(String name1) {
        name = name1
    }
}
