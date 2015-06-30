from java:8

volume /usr/src/groovy-ssh
copy . /usr/src/groovy-ssh
run cd /usr/src/groovy-ssh && ./gradlew -g .gradle shadowJar && cp -a build/libs/gssh.jar /

entrypoint ["java", "-jar", "/gssh.jar"]
