from dockerfile/java:oracle-java7

volume /usr/src/groovy-ssh
copy . /usr/src/groovy-ssh
run cd /usr/src/groovy-ssh && ./gradlew -g .gradle shadowJar && cp -a build/libs/groovy-ssh.jar /

entrypoint ["java", "-jar", "/groovy-ssh.jar"]
