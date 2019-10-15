# DogBreed
Application is built using spring-boot framework.

## Minimum requirements to execute the code
- Java jdk 8 or higher

## Assumptions
1. S3 bucket is already created
2. Access key and Secret key is extracted
3. Database used is MySql. If any other database is used then pom.xml should be edited to include relevent db libraries

## Configuration before execution of the application
Edit application.yml file located in main -> resources folder

app:
  external:
    url: "https://dog.ceo/api/breeds/image/random"
  access-key: aws-access-key
  secret-key: aws-secret-key
  region: region where the s3 bucket is created
  bucket-name: bucket name
  download-dir: temp directory. Please note the file extension for Linux or Windows machine

spring:
  datasource:
    url: jdbc url connector to the DB
    username: Username
    password: Password
    
## Executing the application
Locate DogBreedApplication.java in main -> java -> com -> dog folder and run it as Java application. Spring boot service will
startup with the default tomcat server. Server is hosted on the port 8080
1. GET /breed/generateRecord -> Generates the records and inserts in S3 and DB
2. GET /breed/getRecord/<ID> -> Gets the record from the db. Unique identifier is the ID
3. GET /breed/searchAllBreeds -> Searches all the records in the db
4. DELETE /breed/deleteRecord/<ID> -> Deletes the specific record in S3 and the Db
