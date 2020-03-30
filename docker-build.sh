#!/bin/bash

docker build -t paymenthubee.azurecr.io/phee/camunda-zeebe:0.23.0-alpha2 .
docker push paymenthubee.azurecr.io/phee/camunda-zeebe:0.23.0-alpha2 

docker tag -t paymenthubee.azurecr.io/phee/camunda-zeebe:latest paymenthubee.azurecr.io/phee/camunda-zeebe:0.23.0-alpha2
docker push paymenthubee.azurecr.io/phee/camunda-zeebe:latest

