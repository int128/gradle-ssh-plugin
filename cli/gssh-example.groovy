ssh.remotes {
    tester {
        host = System.getenv('EXT_SSH_HOST') ?: 'localhost'
        user = System.getenv('EXT_SSH_USER') ?: System.getProperty('user.name')
        identity = new File(System.getenv('EXT_SSH_KEY_PATH') ?: "${System.getProperty('user.home')}/.ssh/id_rsa")
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
