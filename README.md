# api-stub
Stub for API

## Build

```bash
$ ./mvnw clean package
```

## Run application

```bash
$ java -jar api-stub-app/target/api-stub.jar
```

## How to access the mock response management screen

[http://localhost:8080/manager/mocks](http://localhost:8080/)

## How to create a new mock response

* Click a "Add a new mock response" button on list screen
* Input a "Requet Path" and "HTTP Method " for API
* Input "HTTP Status Code", "HTTP Headers", "HTTP Body" and "Description" if need
* Click "Create" button

## How to update a mock response

* Click a "Edit" button on list screen
* Update a any item
* Click a "Update" button

## How to delete a mock response

* Click a "Edit" button on list screen
* Click a "Delete" button
* Click a "OK" on confirm dialog

