#!/usr/bin/env sh

set -e

# Database where jobs are persisted
if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for 'POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating dbuserprofile Database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=dbuserprofile --set UP_PASSWORD=${POSTGRES_PASSWORD} <<-EOSQL
  CREATE ROLE :USERNAME WITH LOGIN PASSWORD ':UP_PASSWORD';
  CREATE DATABASE dbuserprofile
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL

echo "Done creating Database dbuserprofile."
