package org.hidetake.gradle.ssh.internal.connection

import com.jcraft.jsch.Logger
import groovy.util.logging.Slf4j

/**
 * A logger which bridges JSch and SLF4J.
 *
 * @author Hidetake Iwata
 */
@Singleton
@Slf4j
class JSchLogger implements Logger {
    @Override
    boolean isEnabled(int logLevel) {
        switch (logLevel) {
            case DEBUG: return log.isDebugEnabled()
            case INFO:  return log.isInfoEnabled()
            case WARN:  return log.isWarnEnabled()
            case ERROR: return log.isErrorEnabled()
            case FATAL: return log.isErrorEnabled()
            default:    return false
        }
    }

    @Override
    void log(int logLevel, String message) {
        switch (logLevel) {
            case DEBUG:
                log.debug("JSch: $message")
                break
            case INFO:
                log.info("JSch: $message")
                break
            case WARN:
                log.warn("JSch: $message")
                break
            case ERROR:
                log.error("JSch: $message")
                break
            case FATAL:
                log.error("JSch: $message")
                break
        }
    }
}
