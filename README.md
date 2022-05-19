# DCSA EBL

![DCSA-EBL MASTER](https://github.com/dcsaorg/DCSA-EBL/actions/workflows/master.yml/badge.svg?branch=master)

Code standard
-------------------------------------
We use [Google Java Style](https://google.github.io/styleguide/javaguide.html), when using
IntelliJ it is recommended to download and activate the
[google-java-format plugin](https://github.com/google/google-java-format).


### BUILDING AND RUNNING THE PROJECT

> **[RECOMMENDED]** Set up a Github Personal Access Token (PAT) as mentioned [here](https://github.com/dcsaorg/DCSA-Core/blob/master/README.md#how-to-use-dcsa-core-packages), then skip to **step 3**.

If you would like to build required DCSA packages individually, begin with step 1.

1) Build **DCSA-Core** as described
   in [DCSA-Core/README.md](https://github.com/dcsaorg/DCSA-Core/blob/master/README.md#to-build-manually-run), then

2) Build **DCSA-Event-Core** as described
   in [DCSA-Event-Core/README.md](https://github.com/dcsaorg/DCSA-Event-Core/blob/master/README.md#to-build-manually-run)
   , then

3) Clone **DCSA-EBL** (with ``--recurse-submodules`` option.) and Build using, ``mvn package``

4) Initialize your local postgresql database as described
   in [datamodel/README.md](https://github.com/dcsaorg/DCSA-Information-Model/blob/master/README.md) \
   or If you have docker installed, you may skip this step and use the docker-compose command mentioned below to set it
   up (This will initialize the application along with the database).

5) Run application,

```
mvn spring-boot:run [options]

options:
 -Dspring-boot.run.arguments="--DB_HOSTNAME=localhost:5432 --AUTH0_ENABLED=false --LOG_LEVEL=DEBUG"
```

OR using **docker-compose**

```
docker-compose up -d -V --build
```

6) Verify if the application is running,

```
curl http://localhost:9090/v2/actuator/health
```

------------------------------------------------------------------------------------------------------------------------

### DEVELOPMENT FLOW

`master` is the main development branch.

`pre-release` and `release` are tagged and should be used as a stable version.

Development continues on `master` and feature branches are created based on `master`.

A typical development flow would look like:

1) Create a feature branch with `master` as base, proceed to make changes to feature branch.
2) Raise PR against `master`. When a PR is raised against master a CI is run to ensure everything is fine.
3) Merge with `master` after approval by at least one verified code owner and successful CI run.

> Note: If changes are required in the `DCSA-Event-Core` or `DCSA-Core`, those changes should first be merged into their respective `master` branches before continuing development in this repository.

4) If development has been completed as per requirements for a given API version, `master` must be tagged to <br>
   create a `release` or `pre-release` accordingly.

When bug fixes are required or changes in pre-release versions are needed, we branch off using the respective <br>
tags and continue development on that branch. It is important to note that these changes must be cherry-picked <br>
and included into `master`.

------------------------------------------------------------------------------------------------------------------------
