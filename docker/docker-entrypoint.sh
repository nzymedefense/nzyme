#!/bin/sh
# Replace environment variables

echo Updating nzyme.conf using .env

echo $DATABASE_URL

envsubst < /etc/nzyme/nzyme.conf.tmp > /etc/nzyme/nzyme.conf

#/bin/sh /usr/share/nzyme/bin/nzyme
# Run the standard container command
exec "$@"
