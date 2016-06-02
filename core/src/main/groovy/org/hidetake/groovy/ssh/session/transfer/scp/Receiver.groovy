package org.hidetake.groovy.ssh.session.transfer.scp

/**
 * A callback interface to receive files or directories via SCP.
 *
 * @author Hidetake Iwata
 */
interface Receiver<T> {

    /**
     * @return true if recursive walker
     */
    boolean isRecursive()

    /**
     * Called when the remote file is found.
     *
     * @param name
     * @param size
     * @param mode
     * @return any object or null
     */
    T foundFile(String name, long size, int mode)

    /**
     * Called when content is received.
     *
     * @param bytes
     * @param context returned by {@link #foundFile(java.lang.String, long, int)}
     */
    void receiveContent(byte[] bytes, T context)

    /**
     * Called when it entered into the remote directory.
     *
     * @param name
     * @param mode
     */
    void enterDirectory(String name, int mode)

    /**
     * Called when it left from the remote directory.
     */
    void leaveDirectory()

}
