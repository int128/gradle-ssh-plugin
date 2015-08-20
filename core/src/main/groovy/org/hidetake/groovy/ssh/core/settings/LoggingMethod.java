package org.hidetake.groovy.ssh.core.settings;

/**
 * Logging method type.
 * Implemented as Java native enum for Gradle 1.x compatibility.
 *
 * @author Hidetake Iwata
 */
public enum LoggingMethod {
    /**
     * Log is sent to SLF4J.
     */
    slf4j,

    /**
     * Log is sent to standard output or error.
     */
    stdout,

    /**
     * Logging is turned off.
     */
    none
}
