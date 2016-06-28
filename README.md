# api-stub
Stub for API

## Build

```bash
$ ./mvnw clean install
```

## Run application

```bash
$ java -jar api-stub-app/target/api-stub.jar
```

## How to use

### How to access the mock response management screen

[http://localhost:8080/](http://localhost:8080/)

### How to create a new mock response

* Click a "Add a new mock response" button on list screen
* Input a "Requet Path" and "HTTP Method " for API
* Input "HTTP Status Code", "HTTP Headers", "HTTP Body" and "Description" if need
* Click "Create" button

### How to update a mock response

* Click a "Edit" button on list screen
* Update a any item
* Click a "Update" button

### How to delete a mock response

* Click a "Edit" button on list screen
* Click a "Delete" button
* Click a "OK" on confirm dialog

## Install api-stub-core into your application

At first, install the `api-stub-core` into local repository. (**Important**)

```bash
$ cd {api-stub-dir}
$ ./mvnw clean install
```

At second, add artifact into your pom file as follow:

```
<dependency>
    <groupId>com.kazuki43zoo</groupId>
    <artifactId>api-stub-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
