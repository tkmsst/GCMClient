#!/bin/sh

APIKEY=Your-API-Key

cnt=3
while read line; do
	for i in `seq $cnt`; do
		RESULT=`curl -k --header "Authorization: key=$APIKEY" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send -d "{\"registration_ids\":[\"$line\"],\"data\":{\"name\":\"$1\",\"num\":\"$2\"}}"`
		if `echo $RESULT | grep -sq "\"success\":1"` || [ $i -eq $cnt ]; then
			break
		fi
		sleep 1
	done
done < `dirname $0`/gcm.dat
