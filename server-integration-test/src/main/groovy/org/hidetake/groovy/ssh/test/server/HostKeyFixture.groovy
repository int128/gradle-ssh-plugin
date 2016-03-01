package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.common.keyprovider.FileKeyPairProvider

import static org.apache.sshd.common.KeyPairProvider.SSH_DSS

class HostKeyFixture {

    static publicKeys(List<String> keyTypes) {
        keyTypes.collect { keyType ->
            HostKeyFixture.getResourceAsStream("/hostkey_${keyType}.pub").text
        }
    }

    static keyPairProvider(List<String> keyTypes = [SSH_DSS]) {
        new FileKeyPairProvider(keyTypes.collect { keyType ->
            HostKeyFixture.getResource("/hostkey_$keyType").file
        } as String[])
    }

}
