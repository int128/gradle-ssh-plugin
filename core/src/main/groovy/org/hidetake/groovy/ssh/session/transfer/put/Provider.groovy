package org.hidetake.groovy.ssh.session.transfer.put

/**
 * An interface of file PUT provider.
 *
 * @author Hidetake Iwata
 */
interface Provider {

    void put(Instructions instructions)

}