---
layout: page
title: Migration Guide (from 1.0.x to 1.1.x)
---

# Migration Guide

This document explains how to migrate from 1.0.x to 1.1.x.


## New features

### Port forwarding

Port forwarding is supported now.
See [the user guide](/user-guide.html#Enable-the-port-forwarding) for details.


### Improvement of DSL extension system

Any private properties and methods of an extension are hidden in the session closure.

```groovy
trait SomeExtension {
  private def someHelper() {
  }
  def something() {
    someHelper()
  }
}
```

```groovy
ssh.run {
  session(remotes.web01) {
    something()   // accessible
    someHelper()  // not accessible form here
  }
}
```

Also we can access to the project instance in an extension.

```groovy
trait SomeExtension {
  def something() {
    // we can access to the project instance here
    project.name
  }
}
```


## No backward compatible change

### DSL extension system

DSL extension system is changed from the class mixin to the trait.

An extension class, for example:

```groovy
class SomeExtension {
  def something() { /* ... */ }
}
ssh.settings {
  extensions << SomeExtension
}
```

should be migrated to a trait:

```groovy
// buildSrc/src/main/groovy/extensions.groovy
trait SomeExtension {
  def something() {
    // we can access to the project instance here (since this version)
    project.name
  }
}
```

```groovy
// build.gradle
ssh.settings {
  extensions << SomeExtension
}
```

All extensions must be placed in the `buildSrc/src/main/groovy` directory.

DSL extension system is no longer supported on Gradle 1.x.
