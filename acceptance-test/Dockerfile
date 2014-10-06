from centos:centos7
run yum install -y openssh-server openssh-clients sudo unzip passwd

# default parameters
env version SNAPSHOT
env jdk     7u60-b19
env gradle  2.1

env ivyRepository /ivy
volume /ivy

add .   /acceptance-test
workdir /acceptance-test
cmd ["./run-test.sh"]
