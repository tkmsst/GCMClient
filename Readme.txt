[Overview]

任意のアプリをプッシュにより起動する。
Launch any application by push.


[Preparation]

下記のサイトでプロジェクト番号と API キーを取得する。
Obtain Project No. and API key at the website below.
https://console.developers.google.com/


[Setting]

Project No :	上記のプロジェクト番号（必須）
Server URL :	Registration ID を受信するサーバーの URL
Activity :	プッシュで起動するアクティビティ名
Action :	プッシュ通知をタップした時に起動するアクション名

Project No :	Project No. obtained above
Server URL :	Server URL receives Registration ID
Activity :	Activity name launched by push
Action :	Action name launched by tapping the notification


[Setting example]

<CSipSiple>
Activity :	com.csipsimple.ui.SipHome
Action :	com.csipsimple.phone.action.CALLLOG

<Zoiper>
Activity :	com.zoiper.android.ui.SplashScreen
Action :	com.zoiper.android.app.action.CALL_LOGS_TAB


[Sample file]

<cURL>
gcm.bat :		Windows 用プッシュ発信スクリプト
gcm.sh :		Linux 用プッシュ発信スクリプト

gcm.bat :		Push script for Windows
gcm.sh :		Push script for Linux

<Asterisk>
extensions.conf :	ダイヤルプラン
extensions2.conf :	固定回線共用ダイヤルプラン

extensions.conf :	Dialplan
extensions2.conf :	Dialplan with a fixed l