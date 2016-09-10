package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Logger
import groovy.util.logging.Slf4j

/**
 * A logger which bridges JSch and SLF4J.
 * It does not redirect DEBUG log because it is too detail and verbose.
 *
 * @author Hidetake Iwata
 */
@Singleton
@Slf4j
class JSchLogger implements Logger {
    private static final ThreadLocal<Boolean> enabledInCurrentThread = new ThreadLocal<>()

    static void setEnabledInCurrentThread(boolean enabled) {
        JSch.logger = JSchLogger.instance
        enabledInCurrentThread.set(enabled)
        log.debug("${enabled ? 'Enabled' : 'Disabled'} JSch logging on ${Thread.currentThread()}")
    }

    @Override
    boolean isEnabled(int logLevel) {
        if (enabledInCurrentThread.get()) {
            switch (logLevel) {
                case INFO:  return log.isDebugEnabled()
                case WARN:  return log.isInfoEnabled()
                case ERROR: return log.isWarnEnabled()
                case FATAL: return log.isErrorEnabled()
                default: return false
            }
        } else {
            false
        }
    }

    @Override
    void log(int logLevel, String message) {
        switch (logLevel) {
            case INFO:
                log.debug("[jsch] $message")
                break
            case WARN:
                log.info("[jsch] $message")
                break
            case ERROR:
                log.warn("[jsch] $message")
                break
            case FATAL:
                log.error("[jsch] $message")
                break
        }
    }
}
