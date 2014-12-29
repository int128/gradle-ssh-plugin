package org.hidetake.groovy.ssh.connection

import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.ProxyType

import static ProxyType.SOCKS

/**
 * Basic validation and defaults for proxied connections created by 
 * {@link DefaultConnectionManager}.
 *  
 * @author mlipper
 *
 */
class ProxyValidator {
	protected static final SOCKS_DEFAULT_VERSION = 5
	protected static final SOCKS_SUPPORTED_VERSIONS = 4..5

	private final Proxy proxy
	private final Map report

	ProxyValidator(Proxy proxy1) {
		this.proxy = proxy1
		this.report = [error:null,warnings:[]]
		createReport()
	}
	
	String error() { report.error }
	
	List<String> warnings() { report.warnings ?: null }
	
	private void createReport() {
		validateProxyType()
		ensureSocksVersion()
		checkCredentials()
	}
	
	private void validateProxyType() {
		if(!ProxyType.values().contains(proxy.type)) {
			report.error = "Unsupported ProxyType ${proxy.type}. Supported types: ${ProxyType.collect {"$it"}.join(', ')}."
		}
	}
	
	private void checkCredentials() {
		// DefaultConnectionManager ignores authentication credentials when 
		// creating proxy server connections unless both proxy.user and 
		// proxy.password are set
		if(proxy.user && !proxy.password) {
			addWarning("proxy.user is set but proxy.password is null. Credentials are ignored for proxy '${proxy.name}'")
		}
		if(!proxy.user && proxy.password) {
			addWarning("proxy.password is set but proxy.user is null. Credentials are ignored for proxy '${proxy.name}'")
		}
	}
	
	private void ensureSocksVersion() {
		def v = proxy.socksVersion
		if(SOCKS == proxy.type && !SOCKS_SUPPORTED_VERSIONS.contains(v)) {
			if(v == 0) {
				addWarning("Using SOCKS v$SOCKS_DEFAULT_VERSION since proxy.socksVersion is not set.")
			} else {
				addWarning("Using SOCKS v$SOCKS_DEFAULT_VERSION since proxy.socksVersion is set to ${proxy.socksVersion} which is not supported by this implementation.")
			}
			proxy.socksVersion = SOCKS_DEFAULT_VERSION
		}
	}

	private void addWarning(String message) {
		report.warnings.add(message)
	}
}
