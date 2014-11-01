from centos:centos7

env HOME /root
run yum install -y openssh-server openssh-clients sudo unzip passwd

# enable sudo without password
run echo '%wheel ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/no-password  && \
    echo 'Defaults !lecture'              > /etc/sudoers.d/no-lecture   && \
    echo 'Defaults !requiretty'           > /etc/sudoers.d/permit-notty

# setup sshd
run sed -i 's/^UsePrivilegeSeparation .*/UsePrivilegeSeparation no/g' /etc/ssh/sshd_config && \
    sed -i 's/^PermitRootLogin .*/PermitRootLogin yes/g' /etc/ssh/sshd_config && \
    touch '/etc/rc.d/init.d/functions' && \
    sshd-keygen

add .   /acceptance-test
workdir /acceptance-test
cmd ["./run-test-on-docker.sh"]
