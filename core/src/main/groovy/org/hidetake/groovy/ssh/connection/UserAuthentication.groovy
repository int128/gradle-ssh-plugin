package org.hidetake.groovy.ssh.connection

trait UserAuthentication {
    /**
     * Remote user.
     */
    String user

    /**
     * Password.
     * Leave as null if the password authentication is not needed.
     */
    String password

    /**
     * Hides credential from result of {@link #toString()}.
     */
    def toString__password() { '...' }

    /**
     * Identity key file for public-key authentication.
     * This must be a {@link File}, {@link String} or null.
     * Leave as null if the public key authentication is not needed.
     */
    def identity

    /**
     * {@link #toString()} formatter to hide credential.
     */
    def toString__identity() { identity instanceof File ? identity : '...' }

    /**
     * Pass-phrase for the identity key.
     * This may be null.
     */
    String passphrase

    /**
     * Hides credential from result of {@link #toString()}.
     */
    def toString__passphrase() { '...' }

    def plus__passphrase(UserAuthentication prior) {
        if (prior.identity == null) {
            if (identity == null) {
                null
            } else {
                passphrase
            }
        } else {
            prior.passphrase
        }
    }

    /**
     * Use agent flag.
     * If <code>true</code>, Putty Agent or ssh-agent will be used to authenticate.
     */
    Boolean agent
}
