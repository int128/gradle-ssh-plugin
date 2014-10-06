#!/bin/bash -xe

test $jdk
test $gradle
test $version
test $ivyRepository
dotssh=/root/.ssh

# setup sshd
sed -i 's/^UsePrivilegeSeparation .*/UsePrivilegeSeparation no/g' /etc/ssh/sshd_config
sed -i 's/^PermitRootLogin .*/PermitRootLogin yes/g' /etc/ssh/sshd_config
touch '/etc/rc.d/init.d/functions'
sshd-keygen

# enable password-less sudo
echo '%wheel ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/no-password
echo 'Defaults !lecture'              > /etc/sudoers.d/no-lecture
echo 'Defaults !requiretty'           > /etc/sudoers.d/permit-notty

# enable public key authentication
mkdir -m 700 -v $dotssh
ssh-keygen -t rsa -N ''            -f $dotssh/id_rsa
ssh-keygen -t rsa -N 'pass_phrase' -f $dotssh/id_rsa_pass
cat $dotssh/id_rsa.pub              > $dotssh/authorized_keys

# generate known hosts
/usr/sbin/sshd
ssh -o StrictHostKeyChecking=no -o HostKeyAlgorithms=ssh-rsa localhost id
ssh-keygen -H -F localhost

# install JDK
curl -L -C - -b oraclelicense=accept-securebackup-cookie -O http://download.oracle.com/otn-pub/java/jdk/$jdk/jdk-${jdk%-*}-linux-x64.rpm
yum install -y ./*.rpm
rm -v *.rpm

# install Gradle
curl -L -O https://services.gradle.org/distributions/gradle-${gradle}-bin.zip
unzip gradle-*-bin.zip
rm -v gradle-*-bin.zip
mv -v gradle-* gradle

# invoke Gradle
/gradle/bin/gradle -i -p acceptance-test -Pversion="$version" -PivyRepository="$ivyRepository" test aggressiveTest

# stop SSH server
xargs kill < /var/run/sshd.pid

