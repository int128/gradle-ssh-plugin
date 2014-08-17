package org.hidetake.gradle.ssh.plugin

/**
 * Represents a connection proxy to use when establishing a {@link Session}. An instance
 * of this class be shared by multiple {@link Remote}s.
 *
 * @author mlipper
 *
 */
class Proxy {
    /**
     * Adds all type of the {@link ProxyType},
     * in order to omit import in a build script.
     */
	static {
        ProxyType.values().each { proxyType ->
            Proxy.metaClass[proxyType.name()] = proxyType
        }
    }

	/**
	 * Name of this instance.
	 */
	final String name

	def Proxy(String name1) {
		name = name1
		assert name
	}

    /**
     * Proxy protocol type
     */
    ProxyType type
	
	/**
	 * SOCKS protocol version. 
	 * This should be set when using ProxyType.SOCKS.
	 */
	int socksVersion

    /**
     * Port.
     */
    int port

    /**
     * Proxy host.
     */
    String host

   /**
     * Proxy user. 
     * This may be null.
     */
    String user    

   /**
     * Proxy password. 
     * This may be null.
     */
    String password

    String toString() {
        "Proxy $name [$host:$port]"
    }
}
