# DCSA EBL

![DCSA-EBL MASTER](https://github.com/dcsaorg/DCSA-EBL/actions/workflows/master.yml/badge.svg?branch=master)

Code standard
-------------------------------------
We use [Google Java Style](https://google.github.io/styleguide/javaguide.html), when using
IntelliJ it is recommended to download and activate the
[google-java-format plugin](https://github.com/google/google-java-format).


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

------------------------------------------------------------------------------------------------------------------------

## Json schemas used for validation

Generated from resolved OAS Yaml file using: https://github.com/Abdiiir/openapi2schema

This will generate a tree of request/response JSON Schemas from the definitions in the OAS Yaml file.

To generate the JSON schemas follow the instructions listed in the [project](https://github.com/Abdiiir/openapi2schema).

The OAS Yaml file can be downloaded at [EBL OpenAPI specification](https://app.swaggerhub.com/apis/dcsaorg/DCSA_EBL).

----------------------------------------

## Example of how to create a schema valiation using GET shipping-instructions endpoint as an example

- Downloading the API:
  - Go to [eBL 2.0.0-beta2 Swagger API](https://app.swaggerhub.com/apis/dcsaorg/DCSA_EBL/2.0.0-Beta-2#/Shipping%20Instructions/get_v2_shipping_instructions__shippingInstructionReference_)
  - Click 'export' in the top-right corner of the page. 
  - Select 'Download API'.
  - Click 'JSON Resolved' to download.

- Generate schemas:
  - Open a terminal in the directory of aforementioned downloaded API file. 
  - Run `openapi2schema -i path/to/api.json > shipping-instruction.json` command.
  - Open `shipping-instruction.json` in a text editor.
  - In the JSON to go `/v2/shipping-instructions/{shippingInstructionReference} > {} get > {} responses > {} 200`.
  - Clip out the JSON segment excluding the 200-brackets and put into a separate JSON file to get your JSON schema.








