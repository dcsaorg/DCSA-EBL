# DCSA EBL

Building and running manually/locally
-------------------------------------

Initialize your local postgresql database as described in datamodel/README.md, then
```
export db_hostname=localhost
export DCSA_Version=0.7.4 #or whatever version is the right one
```
If running without auth0, disable it in src/main/resources/application.yaml

Then build and run with
```
mvn install:install-file -Dfile=../DCSA-Core/target/dcsa_core-$DCSA_Version.jar -DgroupId=org.dcsa -DartifactId=dcsa_core -Dversion=local-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
mvn spring-boot:run -Ddcsa.version=local-SNAPSHOT
```
or using docker-compose
```
mvn package -Ddcsa.version=local-SNAPSHOT
docker-compose up -d -V --build
```

Building and running using docker-compose
-----------------------------------------
To build using DCSA-core from GitHub packages
```
mvn package
docker-compose up -d -V --build
```

Branching and versioning
------------------------
This repository has the following branching and versioning policy:
- The *master* branch always contains the *latest* stable major version.
- Active development on the next major version is performed in the *dev* branch
- Once a new major version is stable the following is done:
  - the now old version is tagged with the version
  - the new version is merged into master

For example, if the latest stable version is 1.x.x the master branch will contain this 1.x.x version. Active development on 2.x.x is being done
on the *dev* branch. When version 2.x.x is released master is tagged with version 1.x.x and the *dev* branch is merged into master  
the result is a tag-1.x.x and master containing the 2.x.x version. 