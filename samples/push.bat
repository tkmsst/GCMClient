set APIKEY=Your-API-Key
set REGID=Your-Registration-ID

curl -k --header "Authorization: key=%APIKEY%" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send -d "{\"registration_ids\":[\"%REGID%\"],\"data\":{\"name\":\"%1\",\"num\":\"%2\"}}"
