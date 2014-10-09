---
layout: default
title: Home
---


Gradle SSH Plugin is
--------------------

A [Gradle](http://www.gradle.org) plugin which provides remote execution and file transfer features for continuous delivery.

See [the User Guide](user-guide.html) for details.


Features
--------

### Integrated with Gradle

Build and deploy seamlessly on Gradle.

```groovy
plugins {
  id 'org.hidetake.ssh' version '{{ site.product.version }}'
}

task deploy(dependsOn: war) << {
  ssh.run {
    // TODO: put a WAR file and reload server
  }
}
```


### Simple and intuitive DSL

* Command execution
* Shell execution
* File transfer

```groovy
ssh.run {
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
plugins {
  id 'org.hidetake.ssh' version '{{ site.product.version }}'
  id 'war'
}

// Global settings
ssh.settings {
  knownHosts = allowAnyHosts    // Disable host key verification
}

// Add a remote host
remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = file('id_rsa')   // Enable public key authentication
  }
}

task deploy(dependsOn: war) << {
  ssh.run {
    settings {
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
}
```

See also [Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template).


Contribution
------------

Gradle SSH Plugin is an open source software developed on the [GitHub project](https://github.com/int128/gradle-ssh-plugin).

Latest release is [version {{ site.product.version }}](https://github.com/int128/gradle-ssh-plugin/releases).
