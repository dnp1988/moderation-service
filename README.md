# Moderation Service
The Moderation Service is a User Flag Application designed to facilitate content moderation on a social network by identifying users who post offensive or abusive messages in the comments section.

The system assigns an offensiveness score to each user message, aggregates these scores per user, and generate a report detailing the average offensiveness score for each user.

## API

The interface provided by the service is a RESTfull API. The operations are as follows.

### POST /api/moderation/messages

Main multipart endpoint that receives an input csv file containing the following structure:
* user_id: (string) The identifier of the user
* message: (string) The message written by the user

It calculates the total amount of messages and average score of each user in the given file.
The input file must not contain an initial title row.

The response is a json containing the ID under which the results are saved.

#### Request example:
```curl
curl --location 'http://localhost:8080/api/moderation/messages' \
     --form 'file="{path_to_local_file}/input.csv"' -k
```

### GET /api/moderation/results/{id}

Result access endpoint that receives a result ID and returns a download stream of the matching file.

The results output csv file contains the following structure:
* user_id: (string) The identifier of the user
* total_messages: (long) The total number of messages written by the user
* avg_score: (double) The average offensiveness score for all the messages of the user

The output file does not contain an initial title row.

#### Request example:
```curl
curl -o output.csv --location 'http://localhost:8080/api/moderation/results/{results_id}' -k
```

## Build

The service is built as a common Maven project with Spring Boot. It can be built using a regular Maven build command.

```
mvn clean package
```

## Run

The service can be run locally as a regular Spring Boot service using the Maven plugin.
A special **mocked** profile is supported to mock the calls to the external services (_Translation_ and _Scoring_) and simulate different responses based on each message.

```
mvn spring-boot:run '-Dspring-boot.run.profiles=mocked'
```

## Tests

The project contains a main integration test named **ModerationIntegrationTest** that covers the main use case of the service and mocks the external services mimicking their behavior.
