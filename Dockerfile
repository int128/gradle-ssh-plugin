from dockerfile/java:oracle-java7

volume /usr/src/groovy-ssh
copy . /usr/src/groovy-ssh
run cd /usr/src/groovy-ssh && \
    ./gradlew --gradle-user-home=.gradle shadowJar && \
    cp -a build/libs/groovy-ssh-SNAPSHOT-all.jar /

entrypoint ["java", "-jar", "/groovy-ssh-SNAPSHOT-all.jar"]
