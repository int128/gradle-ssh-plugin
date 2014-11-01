#!/bin/bash -xe
#
# Requirements:
# - this script must be run as root
# - sshd must be installed
# - below environment variables must be specified

[ "$jdk" ]    || exit 1
[ "$gradle" ] || exit 1

# install JDK
curl -L -C - -b oraclelicense=accept-securebackup-cookie -O \
  http://download.oracle.com/otn-pub/java/jdk/$jdk/jdk-${jdk%-*}-linux-x64.rpm
rpm -i *.rpm
rm -v *.rpm

# install Gradle
curl -L -O https://services.gradle.org/distributions/gradle-${gradle}-bin.zip
unzip gradle-*-bin.zip
rm -v gradle-*-bin.zip
mv -v gradle-* gradle

# start a SSH server
/usr/sbin/sshd

# run the test
./run-test.sh

# stop the SSH server
xargs kill < /var/run/sshd.pid
