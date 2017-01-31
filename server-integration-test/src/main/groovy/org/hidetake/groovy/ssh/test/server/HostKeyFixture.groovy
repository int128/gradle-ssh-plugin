package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.common.util.SecurityUtils

class HostKeyFixture {

    static publicKey(String keyType) {
        HostKeyFixture.getResourceAsStream("/hostkey_${keyType}.pub").text
    }

    static publicKeys(List<String> keyTypes) {
        keyTypes.collect { keyType ->  publicKey(keyType) }
    }

    static keyPairProvider(String... keyTypes) {
        keyPairProvider(keyTypes.toList())
    }

    static keyPairProvider(List<String> keyTypes) {
        def keyPairProvider = SecurityUtils.createClassLoadableResourceKeyPairProvider()
        keyPairProvider.resourceLoader = HostKeyFixture.classLoader
        keyPairProvider.resources = keyTypes.collect { "hostkey_$it".toString() }
        keyPairProvider
    }

}
