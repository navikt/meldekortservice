#!/bin/bash

#
# Oppretter environmentvariabler for brukernavn/passord/config som lagres i Vault.
#

# DB-user meldekortservice
DB_USER_MELDEKORTSERVICE_PATH=/secrets/dbuser/meldekortservicedbuser

if [ -f $DB_USER_MELDEKORTSERVICE_PATH/username ]; then
    export DB_USER_MELDEKORTSERVICE_USERNAME=$(cat $DB_USER_MELDEKORTSERVICE_PATH/username)
fi

if [ -f $DB_USER_MELDEKORTSERVICE_PATH/password ]; then
    export DB_USER_MELDEKORTSERVICE_PASSWORD=$(cat $DB_USER_MELDEKORTSERVICE_PATH/password)
fi

# DB-config meldekortservice
DB_CONFIG_MELDEKORTSERVICE_PATH=/secrets/dbconf/meldekortservicedbconf

if [ -f $DB_CONFIG_MELDEKORTSERVICE_PATH/jdbc_url ]; then
    export DB_CONFIG_MELDEKORTSERVICE_JDBCURL=$(cat $DB_CONFIG_MELDEKORTSERVICE_PATH/jdbc_url)
fi
