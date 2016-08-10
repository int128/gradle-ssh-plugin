ssh.remotes {
    tester {
        host = 'sandbox.127.0.0.1.xip.io'
        port = 8022
        user = 'tester'
        identity = new File('id_rsa')
        knownHosts = new File('known_hosts')
    }
}

ssh.run {
    session(ssh.remotes.tester) {
        put from: ssh.runtime.jar, into: '.'
        execute 'java -jar gssh.jar --version'
        execute 'java -jar gssh.jar --help'
        remove 'gssh.jar'
    }
}
