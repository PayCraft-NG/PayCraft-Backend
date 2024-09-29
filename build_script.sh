#!/usr/bin/env bash
mvn clean install

docker build -t paycraft-development-backend:v1 .

docker login

docker tag paycraft-development-backend:v1 3akare/paycraft-development-backend:v1

docker push 3akare/paycraft-development-backend:v1

#java -jar .\target\paycraft-0.0.1-SNAPSHOT.jar

