from centos:centos7
run curl -L -C - -b oraclelicense=accept-securebackup-cookie -O \
  http://download.oracle.com/otn-pub/java/jdk/7u60-b19/jdk-7u60-linux-x64.rpm && \
  yum install -y ./*.rpm && \
  rm *.rpm

add .   /groovy-ssh
workdir /groovy-ssh
entrypoint ["./gradlew"]
