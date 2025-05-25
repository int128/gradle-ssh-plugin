package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.*
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote

import static org.hidetake.groovy.ssh.core.ProxyType.SOCKS

@Slf4j
trait ProxyConnection {

    void validateProxyConnection(ProxyConnectionSettings settings, Remote remote) {
        if (settings.proxy) {
            def validator = new ProxyValidator(settings.proxy)
            if (validator.error()) {
                throw new IllegalArgumentException(validator.error())
            }
            if (validator.warnings()) {
                validator.warnings().each { warning -> log.info(warning) }
            }
        }
    }

    void configureProxyConnection(JSch jsch, Session session, Remote remote, ProxyConnectionSettings settings) {
        if (settings.proxy) {
            if (settings.proxy.type == SOCKS) {
                if (settings.proxy.socksVersion == 5) {
                    def proxy = new ProxySOCKS5(settings.proxy.host, settings.proxy.port)
                    proxy.setUserPasswd(settings.proxy.user, settings.proxy.password)
                    session.proxy = proxy
                    log.debug("Using SOCKS5 proxy for $remote: $settings.proxy")
                } else {
                    def proxy = new ProxySOCKS4(settings.proxy.host, settings.proxy.port)
                    proxy.setUserPasswd(settings.proxy.user, settings.proxy.password)
                    session.proxy = proxy
                    log.debug("Using SOCKS4 proxy for $remote: $settings.proxy")
                }
            } else {
                def proxy = new ProxyHTTP(settings.proxy.host, settings.proxy.port)
                proxy.setUserPasswd(settings.proxy.user, settings.proxy.password)
                session.proxy = proxy
                log.debug("Using HTTP proxy for $remote: $settings.proxy")
            }
        }
    }

}
