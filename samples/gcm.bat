set APIKEY=Your-API-Key
set REGID=Your-Registration-ID

for /f "usebackq" %%I in ("%~dp0gcm.dat") do (
	curl -k --header "Authorization: key=%APIKEY%" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send -d "{\"registration_ids\":[\"%%I\"],\"data\":{\"name\":\"%1\",\"num\":\"%2\"}}"
)
