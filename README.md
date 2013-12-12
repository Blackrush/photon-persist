photon-persist [![Build Status](https://travis-ci.org/Blackrush/photon-persist.png?branch=master)](https://travis-ci.org/Blackrush/photon-persist)
==============

A JDBC wrapper without any runtime overhead

[Docs](http://blackrush.github.io/photon-persist/)

## How to use

### With gradle

Add these lines to your `build.gradle` file :

```groovy
repositories {
  maven {
    url 'https://raw.github.com/Blackrush/photon-persist/maven-repo'
  }
}

dependencies {
  compile 'org.photon.common:photon-persist:0.1.3'
}
```

### With SBT

(**note yet published**)

```scala
libraryDependencies := Seq(
  "org.photon.common" % "photon-persist" % "0.1.3",
)
```

### With Maven

_todo_
