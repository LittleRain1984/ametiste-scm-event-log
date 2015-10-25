# SCM Event Log Service

## Build Status
[![Build Status](https://travis-ci.org/ametiste-oss/ametiste-scm-event-log.svg?branch=master)](https://travis-ci.org/ametiste-oss/ametiste-scm-event-log)
[![Codacy Badge](https://api.codacy.com/project/badge/370488f10c844437b119002610b5199b)](https://www.codacy.com/app/Ametiste-OSS/ametiste-scm-event-log)
[![codecov.io](https://codecov.io/github/ametiste-oss/ametiste-scm-event-log/coverage.svg?branch=master&precision=2)](https://codecov.io/github/ametiste-oss/ametiste-scm-event-log?branch=master)

## Table Of Content
- [Overview](#overview)
- [Usage](#usage)
  - [Installation](#installation)
  - [Requirements](#requirements)
  - [Configuration properties](#configuration-properties)
- [HTTP API](#http-api)
  - [Operations on Event Informer](#operations-on-event-informer)
    - [Get total count](#get-total-count)
    - [Get event info by Id](#get-event-info-by-id)
    - [Get last N events](#get-last-n-events)
    - [Get events for specific time period](#get-events-for-specific-time-period)
  - [Operations on Event Replayer](#operations-on-event-replayer)
    - [Submit replay task](#submit-replay-task)
    - [Stop replay task](#stop-replay-task)
    - [Get replay task status](#get-replay-task-status)
    - [Remove replay task info](#remove-replay-task-info)
    - [Get active task info](#get-active-task-info)
    - [Get status for all replay tasks](#get-status-for-all-replay-tasks)
    - [Clean status for all completed tasks](#clean-status-for-all-completed-tasks)
- [Logic of operations](#logic-of-operations)
  - [Event Logging Feature](#event-logging-feature)
  - [Event Informer Feature](#event-informer-feature)
  - [Event Replayer Feature](#event-replayer-feature)
  
## Overview

*SCM Event Log* is a part of SCM system infrastructure. Service provides two main feature:
- store all events in persistent storage;
- reply events for specified time period.

It's "default" subscriber of SCM Message Broker that store all history of system changes and can repeat it.
As a storage for events used document-oriented DB: MongoDB. Application also has wide HTTP API to retrieve information from storage and provide replay management. So you can get needed information directly from it.

## Usage

Event Log is Spring Boot application and disctributed as jar file. It placed in `jcenter()` repository:
```
http://jcenter.bintray.com/org/ametiste/scm/scm-event-log/
```
Also releases stored on github or you can build artifact by itself from sources.


#### Installation

Installation process include next steps:

1. Get a ready-to-deploy jar file:
  1. download ready artifact from one of available sources;
  2. build artifact from sources:<br/>project based on Gradle, so to build execute next command from project root directory:<br/><code>gradle build</code>
2. Deploy jar file to the target server.
3. Configure properties for Broker (see [here](#configuration-properties)).
4. Start service by executing:<br/><code>java -jar scm-event-log-[version].jar</code>.

#### Requirements
- JDK 1.8 or higher.

#### Configuration properties

Application has few sets of properties separated by functional modules.

##### Common properties

|Name|Type|Description|Default|
|----|----|-----------|-------|
|`org.ametiste.scm.log.store.flush-period`|integer|Time interval between flush to external storage (in milliseconds).|`1000`|
|`org.ametiste.scm.log.store.allowCreateIndex`|boolean|Allow create indices on initialization.|`true`|
|`org.ametiste.scm.log.replay.bulk-size`|integer|Size of event bulk for sending in one request.|`100`|

##### HttpClient properties

|Name|Type|Description|Default|
|----|----|-----------|-------|
|`org.ametiste.scm.log.sender.client.connect-timeout`|integer|Connection timeout for HTTP client (in milliseconds)|`1000`|
|`org.ametiste.scm.log.sender.client.read-timeout`|integer|Read timeout for HTTP client (in milliseconds)|`1000`|

*Note*: if parameters not defined default values will be used.

##### MongoDB properties
Service use MongoDB for storing events and has next properties:

|Name|Type|Description|Default|
|----|----|-----------|-------|
|`org.ametiste.scm.log.mongo.autoConnectionRetry`|boolean|Enable auto connect retry.|`true`|
|`org.ametiste.scm.log.mongo.connectionsPerHost`|integer|Maximum number of connections per host.|`50`|
|`org.ametiste.scm.log.mongo. threadsAllowedToBlockForConnectionMultiplier`|integer|Multiplier for number of threads allowed to block waiting for a connection.|`5`|
|`org.ametiste.scm.log.mongo.writeConcern`|string|Write concern|`ACKNOWLEDGED`|

To create connection to mongo instance used Spring Boot configuration that that own set of parameters ([link](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html) and [MongoProperties](http://github.com/spring-projects/spring-boot/tree/v1.2.6.RELEASE/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/mongo/MongoProperties.java)).

##### SCM Coordinator properties
Broker use SCM Event Subscriber configuration from Scm Coordinator Library and require define properties for it:

|Name|Type|Description|Default|
|----|----|-----------|-------|
|`spring.application.name`|string|Name of application.|`scm-event-log`|
|`eureka.client.serviceUrl.defaultZone`|URL|URL to Eureka Server instance.<br/>:warning: *Important*: end URL with back slash symbol "/".|`http://localhost:8761/eureka/`|

You can provide and other spring cloud eureka properties for discovering client.

## HTTP API

### Operations on Event Informer
This set of operation provide access to events stored in storage.

#### Get total count

##### Overview
Retrieve count of all stored events in persistent.
```
http://{event log host:port}/info/event/count
```

##### Request Headers
No specific request headers.

##### Request Parameters
No request parameters.

##### Response Format
This operation returns **200 OK** status and *long* number that represent number of total stored in event log persistent.

##### Example
Request:
```
http://localhost/info/event/count HTTP/1.1
```
Response:
```
HTTP/1.1 200 OK
 
150685
```

#### Get event info by Id

##### Overview
Retrieve event information by event id.
```
http://{event log host:port}/info/event/{id}
```

##### Request Headers
No specific request headers.

##### Request Parameters
Request has one path variable parameter:

|Parameter|Type|Description|
|:--------|:---|:----------|
|`id`|UUID|Identifier of event that need to be found.|

##### Response Format
Response is JSON object and contains next fields:

|Field|Type|Description|
|:----|:---|:----------|
|`type`|string|Type of event.|
|`event`|object|Founded event object.|

Event log response with **200 OK** status in any case. When event not founded Log returns fields  filled with *null* value.

##### Example
Request:
```
http://localhost/info/event/c234289c-5453-4c2c-8d95-7a32ee4124ae
```
Response:
```java
HTTP/1.1 200 OK
 
{
  "type":"InstanceStartupEvent",
  "event":{
    "id":"c234289c-5453-4c2c-8d95-7a32ee4124ae",
    "timestamp":124768219,
    "instanceId":"DSEE",
    "version":"0.2.6-1-RC",
    "properties":{
      "server.port":8080
    },
    "nodeId":null,
    "uri":null
  }
}
```

#### Get last N events

##### Overview
Retrieve last N events registered in Event Log.
```
http://{event log host:port}/info/event/last?count={event_count}[&sort={ASC|DESC}]
```

##### Request Headers
No specific request headers.

##### Request Parameters

|Parameter|Type|Description|Default|
|:--------|:---|:----------|:------|
|**`count`**|integer|Number of events that need to retrieve.||
|`sort`|enum|Sort direction by timestamp for result set of events.<br/>Available values: "ASC", "DESC".|`DESC`|

*Note:* :warning: bold marked parameters are required. Other may be ommited (default values will be used).

##### Response Format
Response is JSON array object similar to response in "get event info by id" request.

Response always return 200 OK status. If now events to return response body will contain empty array.

##### Example
Request:
```
http://{event log host:port}/info/event/last?count=3&sort=asc
```
Response:
```java
HTTP/1.1 200 OK
 
 
[
  {
    "type":"InstanceStartupEvent",
    "event":{
      "id":"c156589c-5453-4c45-8d95-7a32ee4124ae",
      "timestamp":1388534400000,
      "instanceId":"RAIN",
      "version":"0.1.0",
      "properties":{
        "server.port":8083
      },
      "nodeId":null,
      "uri":null
    }
  },
  {
    "type":"InstanceStartupEvent",
    "event":{
      "id":"acd6589c-5453-4c45-8d95-7a32ee41eaae",
      "timestamp":1420070400000,
      "instanceId":"BROKER",
      "version":"0.2.6.RELEASE",
      "properties":{
        "server.port":8080
      },
      "nodeId":null,
      "uri":null
    }
  }
]
```

#### Get events for specific time period

##### Overview
Retrieve events registered in Event Log for specified time period.
```
http://{event log host:port}/info/event?[start={start_timestamp}&end={end_timestamp}
                                         &page={page_number}&size={page_size}&sort={ASC|DESC}]
```

##### Request Headers
No specific request headers.

##### Request Parameters

|Parameter|Type|Description|Default|
|:--------|:---|:----------|:------|
|`start`|integer|Start timestamp point in seconds. If set to -1 lower bound<br/>will be omitted.|`-1`|
|`end`|integer|End timestamp point in seconds. If set to -1 upper bound<br/>will be omitted.|`-1`|
|`page`|integer|Zero based page number to receive.|`0`|
|`size`|integer|Number of items in page.|`50`|
|`sort`|enum|Sort direction by timestamp for result set of events.<br/>Available values: "ASC", "DESC".|`DESC`|

##### Response Format
Response is JSON object representation of *org.springframework.data.domain.Page*.

Response always return **200 OK** status.

##### Example
Request:
```
http://{event log host:port}/info/event?page=0&size=50
```
Response:
```java
HTTP/1.1 200 OK
 
 
{
  "content":[
    {
        "type":"InstanceStartupEvent",
        "event":{
            "id":"acd6589c-5453-4c45-8d95-7a32ee41eaae",
            "timestamp":1420070400000,
            "instanceId":"BROKER",
            "version":"0.2.6.RELEASE",
            "properties":{
                "server.port":8080
            },
            "nodeId":null,
            "uri":null
        }
    },{
        "type":"InstanceStartupEvent",
        "event":{
            "id":"c156589c-5453-4c45-8d95-7a32ee4124ae",
            "timestamp":1388534400000,
            "instanceId":"RAIN",
            "version":"0.1.0",
            "properties":{
                "server.port":8083
            },
            "nodeId":null,
            "uri":null
        }
    }
  ],
  "last":true,
  "totalElements":2,
  "totalPages":1,
  "sort":[
    {
        "direction":"DESC",
        "property":"timestamp",
        "ignoreCase":false,
        "nullHandling":"NATIVE",
        "ascending":false
    }
  ],
  "first":true,
  "numberOfElements":2,
  "size":50,
  "number":0
}
```

### Operations on Event Replayer
This set of operation provide replay process manipulation: start/stop replay task, get status of tasks.

#### Submit replay task

##### Overview
Request create replay task with specified event range and destination url. At same time can executes only one task, other tasks stores to unbounded queue.
```java
POST /replay HTTP/1.1
Host: {event log host:port}
Content-Type: application/json
 
{
  "receiverUrl": {target url},
  "startTime": {lower time bound},
  "endTime": {upper time bound}
}
```

##### Request Headers

|Header|Value|
|:-----|:----|
|`Content-Type`|`application/json`|

##### Request Parameters

|Parameter|Type|Description|Default|
|:--------|:---|:----------|:------|
|**`receiverUrl`**|URL|URL to event-receiver endpoint of target service.||
|`startTime`|integer|Start timestamp point in seconds. If set to -1 lower bound will be omitted.|`-1`|
|`endTime`|integer|End timestamp point in seconds. If set to -1 upper bound will be omitted.|`-1`|

*Note:* :warning: bold marked parameters are required. Other may be ommited (default values will be used).

##### Response Format
Response return **200 OK** status if task success registered and *UUID identifier* of task in body. If in some reason task can't be registered service returns **500 INTERNAL SERVER ERROR** and *reason* in body.

##### Example
Request:
```java
POST /replay HTTP/1.1
Host: {event log host:port}
Content-Type: application/json
  
{
  "receiverUrl": "http://localhost:8080/event-receiver",
  "startTime": 1254915505,
  "endTime": 1444915505
}
```
Response:
```java
HTTP/1.1 200 OK
 
"1f91a7a5-916c-439b-8e40-12962c741e1e"
```

#### Stop replay task

##### Overview
Request try stop task if it is in progress or cancel if it's wait for execution.
```
POST /replay/{id}/stop HTTP/1.1
Host: {event log host:port}
```

##### Request Headers
No specific request headers.

##### Request Parameters
Request has one path variable parameter:

|Parameter|Type|Description|
|:--------|:---|:----------|
|`id`|UUID|Replay task identifier.|

##### Response Format
Response return **200 OK** status if task success stopped. If task with this id not exists service returns **404 NOT FOUND** code and *reason* in body.

##### Example
Request:
```
POST /replay/1f91a7a5-916c-439b-8e40-12962c741e1e/stop HTTP/1.1
Host: {event log host:port}
```
Response:
```java
HTTP/1.1 200 OK
```

#### Get replay task status

##### Overview
Request get execution status of  task with specified id.
```
GET /replay/{id}/status HTTP/1.1
Host: {event log host:port}
```

##### Request Headers
No specific request headers.

##### Request Parameters
Request has one path variable parameter:

|Parameter|Type|Description|
|:--------|:---|:----------|
|`id`|UUID|Replay task identifier.|

##### Response Format
Response return **200 OK** status and JSON object with status (*see org.ametiste.scm.log.data.replay.ReplayTaskStatus*). If task with this id not exists service returns **404 NOT FOUND** code and *reason* in body.

##### Example
Request:
```
GET /replay/1f91a7a5-916c-439b-8e40-12962c741e1e/status HTTP/1.1
Host: {event log host:port}
```
Response:
```java
HTTP/1.1 200 OK
 
{ 
    "state":"COMPLETE", 
    "startTime":1254915505000,
    "endTime":1444915505000,
    "totalEvents":2,
    "replayedEvents":2,
    "errorMessage":null,
    "stacktrace":null
}
```

#### Remove replay task info

##### Overview
Remove information about task from registry. It is useful when task complete and we don't want see it in common status.
```
GET /replay/{id}/clean HTTP/1.1
Host: {event log host:port}
```

##### Request Headers
No specific request headers.

##### Request Parameters
Request has one path variable parameter:

|Parameter|Type|Description|
|:--------|:---|:----------|
|`id`|UUID|Replay task identifier.|

##### Response Format
Response return **200 OK** status. If task with this id not exists service returns **404 NOT FOUND** code and *reason* in body.

##### Example
Request:
```
GET /replay/1f91a7a5-916c-439b-8e40-12962c741e1e/clean HTTP/1.1
Host: {event log host:port}
```

Response:
```java
HTTP/1.1 200 OK
```

#### Get active task info

##### Overview
Receive information about that service has active task and id if it's exist.
```
http://{event log host:port}/replay/active 
```

##### Request Headers
No specific request headers.

##### Request Parameters
No request parameters.

##### Response Format
Response return **200 OK** status and information about active task. If service don't have active task it return JSON with null field values.

##### Example
Request:
```
http://{event log host:port}/replay/active
```

Response:
```java
HTTP/1.1 200 OK
 
{
  "hasActive": false,
  "taskId": null
}
```

#### Get status for all replay tasks

##### Overview
Receive information about status for all registered tasks.
```
http://{event log host:port}/replay/status 
```

##### Request Headers
No specific request headers.

##### Request Parameters
No request parameters.

##### Response Format
Response return **200 OK** status and map with task id as key and replay tasks status as value (see [Get replay task status](#get-replay-task-status)).

##### Example
Request:
```
http://{event log host:port}/replay/status
```
Response:
```java
HTTP/1.1 200 OK
 
{
    "3cc15103-0d70-4a9c-9edb-87e46e4e2277": {
        "state": "ERROR",
        "startTime": 0,
        "endTime": 1444915505000,
        "totalEvents": 2,
        "replayedEvents": 0,
        "errorMessage": "Connect to localhost:8085 [localhost/127.0.0.1] failed: Connection refused",
        stacktrace: [...]
    },
    "1f91a7a5-916c-439b-8e40-12962c741e1e": {
        "state": "COMPLETE",
        "startTime": 1254915505000,
        "endTime": 1444915505000,
        "totalEvents": 2,
        "replayedEvents": 2,
        "errorMessage": null,
        "stacktrace": null
    }
}
```

#### Clean status for all completed tasks

##### Overview
Clean information for all completed tasks.
```
http://{event log host:port}/replay/clean 
```

##### Request Headers
No specific request headers.

##### Request Parameters
No request parameters.

##### Response Format
Response return **200 OK** status.

##### Example
Request:
```
http://{event log host:port}/replay/clean
```

Response:
```java
HTTP/1.1 200 OK
```

## Logic of operation
`TODO: add test description`

### Event Logging Feature
![log-logging-diagram](https://cloud.githubusercontent.com/assets/11256858/10718796/69d38304-7b83-11e5-99b5-3a96a90415ab.png)

### Event Informer Feature
![log-informer-diagram](https://cloud.githubusercontent.com/assets/11256858/10718803/83fe9192-7b83-11e5-854c-65a78d53e7b6.png)

### Event Informer Feature
![log-replayer-diagram](https://cloud.githubusercontent.com/assets/11256858/10718807/93cd1e86-7b83-11e5-952d-2b0721871199.png)
