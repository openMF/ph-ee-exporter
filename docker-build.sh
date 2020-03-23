#!/bin/bash

docker build -t paymenthubee.azurecr.io/phee/camunda-zeebe:0.22.1 .
docker push paymenthubee.azurecr.io/phee/camunda-zeebe:0.22.1 

