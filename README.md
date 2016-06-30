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

### How to access mock api

```bash
$ curl -D - http://localhost:8080/api/v1/members
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
x-correlation-id: f8b9eab7-b18a-4713-8910-88ad719ccb86
Content-Length: 0
Date: Thu, 30 Jun 2016 04:03:06 GMT

```

## Install api-stub-core into your application

Install the `api-stub-core` into local repository. (**Important**)

```bash
$ cd {api-stub-dir}
$ ./mvnw clean install
```

Add the `api-stub-core` into your pom file as follow:

```xml
<dependency>
    <groupId>com.kazuki43zoo</groupId>
    <artifactId>api-stub-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Database

You can access the database dilectlly.

### Using H2 admin console

[http://localhost:8080/h2-console/](http://localhost:8080/h2-console/)

| Item | Value |
| ---- | ----- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:~/db/api-stub` |
| User Name | `sa` |
| Password | |

### Using JDBC Driver

Please download jdbc driver on [here](http://repo2.maven.org/maven2/com/h2database/h2/1.4.191/h2-1.4.191.jar).

| Item | Value |
| ---- | ----- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:tcp://localhost:9092/~/db/api-stub` |
| User | `sa` |
| Password | |

