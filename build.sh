#!/bin/sh
./mvnw clean install && docker build -t botica-bot-fishbowl-manager .
