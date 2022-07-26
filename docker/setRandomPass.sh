#!/bin/bash

# Generate new passwords
PASS_WEB=`< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c25`
PASS_DB=`< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c25`

# Replace DB password in files
sed -i 's/NEW_PASSWORD/'$PASS_DB'/' .env

# Gnerate sha256 password and replace WEB password in file
PASS_WEB_256=`echo -n $PASS_WEB | sha256sum | awk '{print $1}'`
sed -i "s/95d30169a59c418b52013315fc81bc99fdf0a7b03a116f346ab628496f349ed5/$PASS_WEB_256/" .env

echo "Your new web password is $PASS_WEB"
