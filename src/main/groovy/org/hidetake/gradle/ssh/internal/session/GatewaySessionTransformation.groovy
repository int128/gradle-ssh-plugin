package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Transformation for gateway session support.
 *
 * @author hidetake.org
 */
class GatewaySessionTransformation {
    protected static final LOCALHOST = '127.0.0.1'
    protected static final PORT_TBD = 0

    /**
     * Port forwarding tunnel.
     */
    static class Tunnel {
        final Remote endpoint
        final SessionSpec spec

        Tunnel(Remote target, Remote gateway) {
            endpoint = new Remote(target.name, target.user, PORT_TBD, LOCALHOST, null,
                                  target.password, target.identity, target.passphrase,
                                  target.agent, target.roles)
            spec = new SessionSpec(gateway, {
                endpoint.port = forwardLocalPortTo(target.host, target.port)
            })
        }
    }

    /**
     * Recursive generator of tunnels.
     *
     * e.g.
     * <code><pre>
     *     // Client -> E -> F -> G -> T
     *     assert target == T
     *     assert target.gateway == G
     *     assert target.gateway.gateway == F
     *     assert target.gateway.gateway.gateway == E
     *     def tunnelT = new Tunnel(target, tunnelG.endpoint)
     *     def tunnelG = new Tunnel(target.gateway, tunnelF.endpoint)
     *     def tunnelF = new Tunnel(target.gateway.gateway, target.gateway.gateway.gateway)
     * </pre></code>
     */
    static class Tunnels {
        final List<SessionSpec> sessionSpecs = []
        final Closure<Remote> generator = { Remote target ->
            if (target.gateway) {
                def tunnel = new Tunnel(target, generator(target.gateway))
                sessionSpecs.add(tunnel.spec)
                tunnel.endpoint
            } else {
                target
            }
        }

        static List<SessionSpec> generate(Remote target, Closure operationClosure) {
            def tunnels = new Tunnels()
            def endpoint = tunnels.generator(target)
            tunnels.sessionSpecs << new SessionSpec(endpoint, operationClosure)
        }
    }

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
                Tunnels.generate(spec.remote, spec.operationClosure)
            } else {
                spec
            }
        }.flatten() as List<SessionSpec>
    }
}
