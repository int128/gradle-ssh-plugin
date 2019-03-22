ssh.remotes {
    tester {
        host = 'localhost'
        port = 22
        user = 'tester'
        identity = new File('os-integration-test/etc/ssh/id_rsa')
        knownHosts = addHostKey(new File('cli/build/known_hosts'))
    }
}

ssh.run {
    session(ssh.remotes.tester) {
        execute 'uname -a'
    }
}

assert new File('cli/build/known_hosts').readLines().any { line ->
    line.startsWith('localhost ssh-rsa')
}
