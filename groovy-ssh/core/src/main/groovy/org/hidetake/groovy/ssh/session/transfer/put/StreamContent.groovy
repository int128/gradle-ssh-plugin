package org.hidetake.groovy.ssh.session.transfer.put

import groovy.transform.ToString

@ToString
class StreamContent {
    final String name
    final InputStream stream

    def StreamContent(String name1, InputStream stream1) {
        name = name1
        stream = stream1
    }
}
