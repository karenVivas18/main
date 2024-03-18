#!/bin/bash

set -x

##GENERAL#VARIABLES##
if [ -z "$ENV" ]
then
export ENV=dev
fi

if [ -z "$BROWSER" ]
	then
        echo "BROWSER is empty, it will use chrome"
        export BROWSER="chrome"
    else
        export BROWSER=$BROWSER
fi

##COMPILE##
if [ -z "$TAG" ]
    then
        echo "TAG is empty it will only compile"
        mvn clean -U test-compile -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

        PROCESS_RESULT=$?

        if [ $PROCESS_RESULT  -eq 0 ]
        then
            echo "Success"
            exit 0
        else
            echo "Failure" >&2
            exit 1
        fi
    else
        export TAG=$TAG
fi


##SETTINGS#TO#RUN##
echo "TAG=$TAG ENV=$ENV BROWSER=$BROWSER"

mvn clean -Dremote=true -Denv=$ENV -Dbrowser=$BROWSER -Dmaven.wagon.http.ssl.ignore.validity.dates=true

PROCESS_RESULT=$?

ls -lsa src/test/resources/features

#Report
tar -czvf cucumber-reports.tar.gz cucumber-reports/

tar -czvf allure-results.tar.gz allure-reports/
curl -F "file=@allure-results.tar.gz" l1dev1-mock01:8686/loadAllure

if [ $PROCESS_RESULT  -eq 0 ]
then
	echo "Success"
	exit 0
else
	echo "Failure" >&2
	exit 1
fi
