# relax-sprint-boot-starter

Please read [Relax Stack documention here](https://relax.infilos.com/).

A collection of toolkits to develop spring boot applications.

- relax-java
- relax-scala
- relax-track-rest
- relax-track-audit
- relax-router
- relax-authctx

## Spring-boot version

Version is defined as `2.4.1-0`, `2.4.1` is the spring-boot release version, `-0` means this tookit's build version.

## Contributions

### Version

1. Increase build version: `bash version.sh -b`
2. Change spring-boot release version: `bash version.sh -s 2.3.4`

### Release

- Snapshot: `mvn clean deploy`
- Release: `mvn clean package source:jar gpg:sign install:install deploy:deploy`

