ssh.remotes {
    tester {
        host = 'sandbox.local'
        port = 8022
        user = 'tester'
        identity = new File("${System.getProperty('user.home')}/.ssh/id_ext")
        knownHosts = new File("${System.getProperty('user.home')}/.ssh/known_hosts_ext")
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
