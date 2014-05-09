#!/bin/sh
USER=$1
DB=${2:-$USER}
cat arviointiasteikot.sql | psql -h localhost -U $USER -d $DB -f-

