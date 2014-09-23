---
layout: default
title: Home
---


Gradle SSH Plugin is
--------------------

A [Gradle](http://www.gradle.org) plugin which provides remote execution and file transfer features for continuous delivery.

An open source software developed on [GitHub project](https://github.com/int128/gradle-ssh-plugin).

See also [User Guide](user-guide.html) for details.


Features
--------

### Integrated with Gradle

Build and deploy seamlessly on Gradle.

```groovy
apply plugin: 'war'
apply plugin: 'ssh'

task deploy(type: SshTask, dependsOn: war) {
  // TODO: put a WAR file and reload server
}
```


### Simple and intuitive DSL

* Command execution
* Shell execution
* File transfer

```groovy
task deploy(type: SshTask, dependsOn: war) {
  session(remotes.webServer) {
    put war.archivePath, '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```


### Authentication and security

* Password authentication
* Public key authentication with an OpenSSH key pair
* Authentication with Putty Agent or ssh-agent
* Host key verification with an OpenSSH style `known_hosts` file


### Also supports

* Connection via gateways or proxy
* Providing a password for `sudo` command
* Interaction with the shell such as bash or Cisco IOS


Example
-------

Here is an example for typical deployment scenario.

```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:{{ site.product.version }}'
  }
}

apply plugin: 'org.hidetake.ssh'


// Global settings
ssh {
  identity = file('id_rsa')     // Enable public key authentication
  knownHosts = allowAnyHosts    // Disable host key verification
}

// Add a remote host
remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
  }
}

task deploy(type: SshTask, dependsOn: war) {
  description = 'Deploys an application to the server.'
  ssh {
    // Enable PTY allocation for sudo
    pty = true
  }
  session(remotes.webServer) {
    // Put a built WAR to the server
    put war.archivePath, '/webapps'
    // Restart the application server
    execute 'sudo service tomcat restart'
  }
}
```

See also [Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template).


Contribution
------------

Please send your issue report or pull request via [GitHub project](https://github.com/int128/gradle-ssh-plugin).

Latest release is [version {{ site.product.version }}](https://github.com/int128/gradle-ssh-plugin/releases).
