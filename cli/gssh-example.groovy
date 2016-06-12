ssh.remotes {
    tester {
        host = System.getenv('EXT_SSH_HOST') ?: 'localhost'
        user = System.getenv('EXT_SSH_USER') ?: System.getProperty('user.name')
        identity = new File(System.getenv('EXT_SSH_HOST')
                ? "${System.getProperty('user.home')}/.ssh/id_ext"
                : "${System.getProperty('user.home')}/.ssh/id_rsa")
        knownHosts = new File(System.getenv('EXT_SSH_HOST')
                ? "${System.getProperty('user.home')}/.ssh/known_hosts_ext"
                : "${System.getProperty('user.home')}/.ssh/known_hosts")
    }
}

ssh.run {
    session(ssh.remotes.tester) {
        put from: ssh.runtime.jar, into: '.'
        execute 'java -jar gssh.jar --version'
        execute 'java -jar gssh.jar --help'
        execute 'sudo yum update -y', pty: true
        remove 'gssh.jar'
    }
}
