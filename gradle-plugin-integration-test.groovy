// Run plugin integration test on Gradle 2 and 1

def randomInt(int max = 10000) {
    (Math.random() * max) as int
}

ssh.remotes {
    ec2 {
        host = System.getenv('INTEGRATION_TEST_SSH_HOST')
        user = System.getenv('INTEGRATION_TEST_SSH_USER')
        identity = new File("${System.getProperty('user.home')}/.ssh/id_rsa")
    }
    tester {
        host = System.getenv('INTEGRATION_TEST_SSH_HOST')
        user = "user${randomInt()}"
        password = "password${randomInt()}"
    }
}

ssh.run {
    session(ssh.remotes.ec2) {
        execute 'sudo yum install -y git', pty: true
        execute "sudo useradd -m ${ssh.remotes.tester.user}", pty: true
        execute "sudo passwd $ssh.remotes.tester.user", pty: true, interaction: {
            when(partial: ~/.+[Pp]assword: */) {
                standardInput << ssh.remotes.tester.password << '\n'
            }
        }
    }
}

try {
    ssh.run {
        session(ssh.remotes.tester) {
            execute 'mkdir -m 700 -p -v .ssh'
            execute 'ssh-keygen -t rsa -N "" -f .ssh/id_rsa'
            execute 'cat .ssh/id_rsa.pub > .ssh/authorized_keys'
            execute 'chmod -v 600 .ssh/authorized_keys'
            execute 'ssh -o StrictHostKeyChecking=no -o HostKeyAlgorithms=ssh-rsa -i .ssh/id_rsa localhost id'
            execute 'ssh-keygen -H -F localhost'

            put from: "${System.getProperty('user.home')}/.m2", into: '.'
            execute 'git clone --depth 1 https://github.com/int128/gradle-ssh-plugin.git'
            execute 'cd gradle-ssh-plugin && sed -i -e "s,groovy-ssh:[0-9.]*,groovy-ssh:' + (System.getenv('TRAVIS_TAG') ?: 'SNAPSHOT') + ',g" build.gradle'
            execute 'cd gradle-ssh-plugin && git diff'

            execute "cd gradle-ssh-plugin && ./gradlew check publishMavenPublicationToMavenLocal"
            execute 'cd gradle-ssh-plugin && TERM=dumb ./gradle/acceptance-test.sh', pty: true
            execute 'cd gradle-ssh-plugin && TERM=dumb ./gradle/acceptance-test.sh 1.12', pty: true
        }
    }
} finally {
    ssh.run {
        session(ssh.remotes.ec2) {
            execute "sudo userdel -r ${ssh.remotes.tester.user}", pty: true
        }
    }
}
