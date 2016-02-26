// Run plugin integration test on Gradle 2 and 1

def randomInt(int max = 10000) {
    (Math.random() * max) as int
}

ssh.remotes {
    ec2 {
        host = System.getenv('INTEGRATION_TEST_SSH_HOST')
        user = System.getenv('INTEGRATION_TEST_SSH_USER')
        identity = new File("${System.getProperty('user.home')}/.ssh/id_ec2")
    }
    tester1 {
        role 'testers'
        host = System.getenv('INTEGRATION_TEST_SSH_HOST')
        user = "user${randomInt()}"
        identity = new File("${System.getProperty('user.home')}/.ssh/id_ec2")
    }
    tester2 {
        role 'testers'
        host = System.getenv('INTEGRATION_TEST_SSH_HOST')
        user = "user${randomInt()}"
        identity = new File("${System.getProperty('user.home')}/.ssh/id_ec2")
    }
}

ssh.run {
    session(ssh.remotes.ec2) {
        execute 'sudo yum install -y git', pty: true
    }
}

try {
    ssh.run {
        session(ssh.remotes.ec2) {
            ssh.remotes.role('testers').each { tester ->
                execute """
sudo useradd -m $tester.user
sudo -i -u $tester.user mkdir -m 700 .ssh
sudo -i -u $tester.user tee .ssh/authorized_keys < .ssh/authorized_keys
sudo -i -u $tester.user chmod 600 .ssh/authorized_keys
""", pty: true
            }
        }
        session(ssh.remotes.role('testers')) {
            executeBackground '''
ssh-keygen -t rsa -N "" -f .ssh/id_rsa
cat .ssh/id_rsa.pub >> .ssh/authorized_keys
ssh -o StrictHostKeyChecking=no -o HostKeyAlgorithms=ssh-rsa -i .ssh/id_rsa localhost id
ssh-keygen -H -F localhost
git clone --depth 1 https://github.com/int128/gradle-ssh-plugin
'''
            put from: "${System.getProperty('user.home')}/.m2", into: '.'
        }
    }
    ssh.run {
        session(ssh.remotes.tester1) {
            executeBackground 'cd gradle-ssh-plugin && TERM=dumb ./gradle/acceptance-test.sh 1.12', pty: true
        }
        session(ssh.remotes.tester2) {
            executeBackground 'cd gradle-ssh-plugin && TERM=dumb ./gradle/acceptance-test.sh', pty: true
        }
    }
} finally {
    ssh.run {
        session(ssh.remotes.ec2) {
            ssh.remotes.role('testers').each { tester ->
                execute "sudo userdel -r $tester.user", pty: true
            }
        }
    }
}
