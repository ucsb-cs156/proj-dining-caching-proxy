# Updating Versions of Java and/or node

## Updating the Java version

When updating the version of Java used, the following places need to be adjusted:

* `Versions` section of the README.md
* `pom.xml` file
* `.java-version` file (used by Github Actions scripts)
* `Dockerfile` used for deploying on Dokku (the `openjdk-NN-jdk` apt package and `JAVA_HOME`)

In addition, any Maven plugin that **reads compiled `.class` files** must support the
new Java class-file format, or it fails at run time even though the code compiles.
Check these versions in `pom.xml` and bump them if needed:

* `jacoco-maven-plugin` (test coverage)
* `pitest-maven` and `pitest-junit5-plugin` (mutation testing)
* `git-code-format-maven-plugin` and the `google-java-format` it bundles (formatting)

Also note that since JDK 23, `javac` does not run annotation processors such as Lombok
unless told to explicitly; `pom.xml` sets `<proc>full</proc>` on `maven-compiler-plugin`
for that reason. If a build ever fails with `cannot find symbol` for every Lombok-generated
getter, builder or `log` field, that setting is the first thing to check.

When the Spring Boot version changes, also check libraries tied to the Boot line,
such as `springdoc-openapi-starter-webmvc-ui`.

## Updating the node version

* `Versions` section of the README.md
* `engines` section in `frontend/package.json` (this is used by Github Actions scripts)
* `frontend/.nvmrc`
* `Dockerfile` used for deploying on Dokku
* `pom.xml` in the configuration of `frontend-maven-plugin` (adjust the `app.frontend.nodeVersion` property)
