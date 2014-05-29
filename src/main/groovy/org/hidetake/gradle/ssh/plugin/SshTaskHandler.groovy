package org.hidetake.gradle.ssh.plugin

/**
 * An interface delegating into {@link SshTask}.
 *
 * @author hidetake.org
 */
interface SshTaskHandler {
    /**
     * Configure task specific settings.
     *
     * @param closure closure for {@link CompositeSettings}
     */
    void ssh(Closure closure)

    /**
     * Add a session.
     *
     * @param remote the {@link org.hidetake.gradle.ssh.plugin.Remote}
     * @param closure closure for {@link org.hidetake.gradle.ssh.plugin.session.SessionHandler} (run in execution phase)
     */
    void session(Remote remote, Closure closure)

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link org.hidetake.gradle.ssh.plugin.session.SessionHandler} (run in execution phase)
     */
    void session(Collection<Remote> remotes, Closure closure)

    /**
     * Add a session.
     * This method creates a {@link Remote} instance and add a session with it.
     *
     * @param remoteProperties properties of a {@link org.hidetake.gradle.ssh.plugin.Remote}
     * @param closure closure for {@link org.hidetake.gradle.ssh.plugin.session.SessionHandler} (run in execution phase)
     */
    void session(Map remoteProperties, Closure closure)

    /**
     * Execute the task.
     * This method should be called by plugin internally.
     *
     * @param globalSettings
     * @return returned value of the last session
     */
    Object execute(CompositeSettings globalSettings)
}
