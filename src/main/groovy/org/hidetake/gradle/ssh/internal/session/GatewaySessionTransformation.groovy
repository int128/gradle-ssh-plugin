package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Transformation for gateway session support.
 *
 * @author hidetake.org
 */
@Singleton
class GatewaySessionTransformation {
    protected static final LOCALHOST = '127.0.0.1'

    /**
     * Applies transformation.
     * <p>
     * If the session has remote gateway property,
     * a forwarder session is inserted before the session.
     *
     * @param specs list of session spec
     * @return transformed list
     */
    static List<SessionSpec> transform(List<SessionSpec> specs) {
        specs.collect { spec ->
            if (spec.remote.gateway) {
                def forwardedRemote = new Remote(
                        spec.remote.name,
                        spec.remote.user,
                        0,
                        LOCALHOST,
                        null,
                        spec.remote.password,
                        spec.remote.identity,
                        spec.remote.passphrase,
                        spec.remote.agent,
                        spec.remote.roles)
                def forwarder = new SessionSpec(spec.remote.gateway, {
                    forwardedRemote.port = forwardLocalPortTo(spec.remote.host, spec.remote.port)
                })
                def forwarded = new SessionSpec(forwardedRemote, spec.operationClosure)
                [forwarder, forwarded]
            } else {
                spec
            }
        }.flatten() as List<SessionSpec>
    }
}
