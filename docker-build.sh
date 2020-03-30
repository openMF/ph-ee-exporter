#!/bin/bash

docker build -t paymenthubee.azurecr.io/phee/camunda-zeebe:0.23.0-alpha2 .
docker push paymenthubee.azurecr.io/phee/camunda-zeebe:0.23.0-alpha2 

docker tag paymenthubee.azurecr.io/phee/camunda-zeebe:0.23.0-alpha2 paymenthubee.azurecr.io/phee/camunda-zeebe:latest
docker push paymenthubee.azurecr.io/phee/camunda-zeebe:latest

