#!/usr/bin/env sh

set -e

# Database where jobs are persisted
if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for 'POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating dbuserprofile Database . . . "

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE dbuserprofile WITH PASSWORD 'dbuserprofile';
  CREATE DATABASE dbuserprofile ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE dbuserprofile TO dbuserprofile;
  ALTER ROLE dbuserprofile WITH LOGIN;
EOSQL


echo "Done creating Database dbuserprofile."
