package org.hidetake.groovy.ssh.session.transfer.get
/**
 * An interface of file GET provider.
 *
 * @author Hidetake Iwata
 */
interface Provider {

    void get(String remotePath, RecursiveReceiver receiver)

    void get(String remotePath, FileReceiver receiver)

    void get(String remotePath, StreamReceiver receiver)

}