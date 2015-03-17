#!/bin/sh

APIKEY=Your-API-Key
REGID=Your-Registration-ID

cnt=5
for i in `seq $cnt`
do
	RESULT=`curl -k --header "Authorization: key=$APIKEY" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send -d "{\"registration_ids\":[\"$REGID\"],\"data\":{\"name\":\"$1\",\"num\":\"$2\"}}"`
	if echo $RESULT | grep "\"success\":1"; then
		break
	fi
	if [ $i -lt $cnt ]; then
		sleep 1
	fi
done
