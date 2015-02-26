---
layout: page
title: Migration Guide (from 1.0.x to 1.1.x)
---

# Migration Guide

This document explains how to migrate from 1.0.x to 1.1.x.


## New features

### Port forwarding

[Port forwarding](/user-guide.html#Enable-the-port-forwarding) is supported now.


### Map based DSL extension system

We can extend DSL with a map of method name and implementation.
Following example adds the method `restartAppServer`.

```groovy
ssh.settings {
  extensions.add restartAppServer: {
    execute "/opt/${project.name}/tomcat/bin/shutdown.sh"
    execute "/opt/${project.name}/tomcat/bin/startup.sh"
  }
}

ssh.run {
  session(ssh.remotes.testServer) {
    restartAppServer()
  }
}
```


## No backward compatible change

### Class based DSL extension system

Any extension classes in the build script will no longer work.
They must be placed in the `buildSrc/src/main/groovy` directory.

So we recommend to use the map based extension instead of the class based extension.

For example, following extension:

```groovy
// buildSrc/src/main/groovy/extensions.groovy
class TomcatExtension {
  def restartAppServer() {
    execute "/opt/${project.name}/tomcat/bin/shutdown.sh"
    execute "/opt/${project.name}/tomcat/bin/startup.sh"
  }
}
```

can be migrated to:

```groovy
// build.gradle
ssh.settings {
  extensions.add restartAppServer: {
    execute "/opt/${project.name}/tomcat/bin/shutdown.sh"
    execute "/opt/${project.name}/tomcat/bin/startup.sh"
  }
}
```
