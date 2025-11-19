#!/bin/bash

# Get profiles from command line argument, default to "default,local"
PROFILES="${1:-default,local}"

echo "Starting EperusteetApplication with profiles: $PROFILES"

SPRING_PROFILES="-Dspring.profiles.active=$PROFILES"

export MAVEN_OPTS="$VM_ARGS $SPRING_PROFILES"

mvn -f eperusteet/eperusteet-service spring-boot:run -Dspring-boot.run.profiles=$PROFILES


