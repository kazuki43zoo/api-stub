
[![Build Status](https://travis-ci.org/kazuki43zoo/api-stub.svg?branch=master)](https://travis-ci.org/kazuki43zoo/api-stub)
[![Dependency Status](https://www.versioneye.com/user/projects/57dcb1dc037c200040cdcef9/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57dcb1dc037c200040cdcef9)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4ff22966f0a848fa9a880fd1fc0f50e6)](https://www.codacy.com/app/kazuki43zoo/api-stub?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=kazuki43zoo/api-stub&amp;utm_campaign=Badge_Grade)

# api-stub
Stub for API

## Requirements for Maven compile and runtime

* Java SE 8 (JDK 1.8)


## Getting started !!

* Download the [api-sutb.jar](https://github.com/kazuki43zoo/api-stub/releases/download/1.0.0.RELEASE/api-stub.jar) and save to any directory

* Run the application
  
  ```bash
  $ java -jar api-stub.jar
  ```

  > **Note: How to change server port**
  > 
  > Default server port is `8080`.
  > If you want to change server port, please run application as follow (adding `--server.port=xxxx`):
  > 
  > e.g.)
  > 
  > ```bash
  > $ java -jar api-stub.jar --server.port=9999
  > ```

* Stop the application (Type "Ctrl + C")


## Try to access mock API

### Default response (200 OK)

* Access to an unknown path

  ```bash
  $ curl -D - http://localhost:8080/api/v1/test
  HTTP/1.1 200 
  x-correlation-id: 0d04375f-92ec-4f59-8a0b-78bfc542a7a8
  Content-Length: 0
  Date: Sun, 23 Oct 2016 07:20:45 GMT
  
  ```

### Mock response

* Open the management screen

  [http://localhost:8080/](http://localhost:8080/)

  ![List for mock response](material/list-screen.png)

* Add a new mock response

  ![Add for mock response](material/create-screen.png)

* Access to the creating path

  ```bash
  $ curl -D - http://localhost:8080/api/v1/members/1
  HTTP/1.1 200 
  x-correlation-id: 9b8c538c-a3fd-478e-b62c-d0116c44c0ba
  Content-Type: application/json;charset=UTF-8
  Transfer-Encoding: chunked
  Date: Sun, 23 Oct 2016 07:36:58 GMT
  
  {
      "name" : "kazuki43zoo"
  }
  ```

## Evidence

Evidence(request headers, request parameters, request body and upload files) are outoutting the `evidence` directory on your application root.

  ```text
  ${WORKING_DIR}/evidence
  └─api
     └─v1
       └─members
           └─1 (* request path)
             └─GET (* http method)
                └─20161023163658842_9b8c538c-a3fd-478e-b62c-d0116c44c0ba (* ${datetime}_${x-correlation-id})
                    + request.json (* include http headers, request parameters)
                    + body.txt (* request body)
                    + uploadFile_01_xxxx.png (* upload files)
  ```

## Console logs

Access logs are outputting the console.

```text
...
2016-10-23 16:36:58.842  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : Start.
2016-10-23 16:36:58.842  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : Evidence Dir : /Users/xxxx/api-stub/evidence/api/v1/members/1/GET/20161023163658842_9b8c538c-a3fd-478e-b62c-d0116c44c0ba
2016-10-23 16:36:58.843  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : Request      : {"parameters":{},"headers":{"host":["localhost:8080"],"user-agent":["curl/7.43.0"],"accept":["*/*"]}}
2016-10-23 16:36:58.843  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : Request body : empty
2016-10-23 16:36:58.844  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : Mock Response is 1.
2016-10-23 16:36:58.845  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : Response     : {"httpStatus":"OK","headers":{"Content-Type":["application/json"],"x-correlation-id":["9b8c538c-a3fd-478e-b62c-d0116c44c0ba"]}}
2016-10-23 16:36:58.845  INFO 48685 --- [nio-8080-exec-1] GET /api/v1/members/1                    : End.
...
```

## Access the database

You can access the database directly.

### Using H2 admin console

[http://localhost:8080/h2-console/](http://localhost:8080/h2-console/)

| Item | Value |
| ---- | ----- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:~/db/api-stub` |
| User Name | `sa` |
| Password | |

### Using JDBC Driver on client tool

Please download jdbc driver on [here](http://repo2.maven.org/maven2/com/h2database/h2/1.4.193/h2-1.4.193.jar).

| Item | Value |
| ---- | ----- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:tcp://localhost/~/db/api-stub` |
| User | `sa` |
| Password | |


## Appendix

### Requirements for IDE

#### Install the Lombok

If you use a IDE(STS, IDEA, etc...), please install the lombok. About how to install the lombok, please see as follows:

* http://jnb.ociweb.com/jnb/jnbJan2010.html#installation
* https://projectlombok.org/download.html

#### Groovy Eclipse

If you use a STS(or Eclipse), please install the Groovy Eclipse plugin. About how install the Groovy Eclipse, please see as follow:

* https://github.com/groovy/groovy-eclipse/wiki

### How to build 

* Clone the project

  ```bash
  $ git clone https://github.com/kazuki43zoo/api-stub.git
  ```

* Build a jar

  ```bash
  $ cd api-stub
  $ ./mvnw clean install
  ```

* Run the application using building jar

  ```bash
  $ java -jar api-stub-app/target/api-stub.jar
  ```


## Known Issues

* https://github.com/kazuki43zoo/api-stub/issues

## Feature request and bug report

* https://github.com/kazuki43zoo/api-stub/issues/new

