#!/bin/bash
psql -U postgres -c "DROP DATABASE test;"
psql -U postgres -c "CREATE DATABASE test;"
psql -U postgres -c "CREATE USER test WITH PASSWORD 'test';"
psql -U postgres -c "GRANT ALL ON DATABASE test TO test;"
