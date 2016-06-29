package org.hidetake.groovy.ssh.session.transfer.get

interface WritableReceiver {

    void write(byte[] bytes)

}
