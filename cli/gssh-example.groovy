ssh.remotes {
    tester {
        host = 'localhost'
        port = 8022
        user = 'tester'
        identity = new File('.ssh/id_rsa')
        knownHosts = addHostKey(new File('.ssh/known_hosts'))
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

assert new File('.ssh/known_hosts').readLines().any { line ->
    line.startsWith('[localhost]:8022 ssh-rsa')
}
