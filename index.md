---
layout: default
title: Home
---

<section class="jumbotron">
<section class="container">
<section class="row">
<section class="col-md-10">

# Deploy your App from Gradle

A Gradle plugin which provides SSH facilities for continuous delivery.

</section>
<section class="col-md-2">

![Logo](/public/gradle-ssh-plugin.png)

</section>
</section>
<a class="github-button" href="https://github.com/int128/gradle-ssh-plugin" data-icon="octicon-star" data-count-href="/int128/gradle-ssh-plugin/stargazers" data-count-api="/repos/int128/gradle-ssh-plugin#stargazers_count">Star</a>
</section>
</section>
<section class="container">
<section class="row">
<section class="col-md-6">

## Features

See also [User Guide](/user-guide.html) for details.

### Integrated with Gradle

* Run SSH sessions in the task
* Deploy artifacts after build

### Authentication and Security

* Password authentication
* Public key authentication
* Putty Agent or OpenSSH agent support
* Provide a password for sudo prompt
* Host key verification with known_hosts
* Connect via SSH gateways
* Connect via SOCKS or HTTP proxy

### Interaction

* Interact with the remote command
* Handle standard output and error
* Write to standard input

</section>
<section class="col-md-6">

## Getting Started

Here is an example for typical deployment scenario.

```groovy
plugins {
  id 'org.hidetake.ssh' version '{{ site.product.version }}'
}

remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = file('id_rsa')
  }
}

task deploy << {
  ssh.run {
    session(remotes.webServer) {
      put 'example.war', '/webapps'
      execute 'sudo service tomcat restart'
    }
  }
}
```

See also [Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template).

</section>
</section>


Contribution
------------

Gradle SSH Plugin is an open source software developed on the [GitHub project](https://github.com/int128/gradle-ssh-plugin).

</section>
