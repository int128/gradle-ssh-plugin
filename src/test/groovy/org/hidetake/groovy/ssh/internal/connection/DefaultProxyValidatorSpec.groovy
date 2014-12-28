package org.hidetake.groovy.ssh.internal.connection

import org.hidetake.groovy.ssh.api.Proxy
import spock.lang.Specification

import static org.hidetake.groovy.ssh.api.ProxyType.HTTP
import static org.hidetake.groovy.ssh.api.ProxyType.SOCKS

class DefaultProxyValidatorSpec extends Specification {
	
	def "proxy with user and password generates no warnings"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = HTTP
			proxy.user = "jsmith"
			proxy.password = "s3cr3t"
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs == null
	}
	
	def "proxy without user or password generates no warnings"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = HTTP
			proxy.user = null
			proxy.password = null
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs == null
	}
	
	def "proxy with user but no password generates warning"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = HTTP
			proxy.user = "jsmith"
			proxy.password = null
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs.size() == 1
	}
	
	def "proxy with password but no user generates warning"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = HTTP
			proxy.user = null
			proxy.password = "s3cr3t"
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs.size() == 1
	}
	
	def "proxy with supported socksVersion generates no warnings"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = SOCKS
			proxy.socksVersion = 5
			proxy.user = "jsmith"
			proxy.password = "s3cr3t"
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs == null
	}
	
	def "proxy with unsupported socksVersion generates warning"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = SOCKS
			proxy.socksVersion = 8
			proxy.user = "jsmith"
			proxy.password = "s3cr3t"
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs.size() == 1
	}
	
	def "proxy without socksVersion generates warning"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = SOCKS
			proxy.user = "jsmith"
			proxy.password = "s3cr3t"
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs.size() == 1
	}
	
	def "proxy with null type is an error"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.user = "jsmith"
			proxy.password = "s3cr3t"
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg != null
			warnMsgs == null
	}
	
	def "proxy without user, password, or socksVersion generates one warning"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = SOCKS
			proxy.user = null
			proxy.password = null
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs.size() == 1
	}

	def "proxy with user, no password, and no socksVersion generates two warnings"() {
		given:
			def proxy = new Proxy("proxy")
			proxy.type = SOCKS
			proxy.user = "jsmith"
			proxy.password = null
			def validator = new ProxyValidator(proxy)
	
		when:
			def errorMsg = validator.error()
			def warnMsgs = validator.warnings()
			
		then:
			errorMsg == null
			warnMsgs.size() == 2
	}
}
