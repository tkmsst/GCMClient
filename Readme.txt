[Overview]

任意のアプリをプッシュにより起動する。
Launch any application from a push notification.


[Preparation]

下記のサイトでプロジェクト番号と API キーを取得する。
Obtain Project No. and API key at the website below.
https://console.developers.google.com/

プッシュにより起動させるアプリをインストールする。
Install an application to be launched from a push notification.


[Setting]

Project No :	上記のプロジェクト番号（必須）
Server URL :	Registration ID を受信するサーバーの URL
Activity :	プッシュにより起動させるアクティビティ名もしくはパッケージ名
Action :	プッシュ通知をタップした時に起動するアクション名

Project No :	Project No. obtained above (mandatory)
Server URL :	Server URL receives Registration ID
Activity :	Activity name or package name to be launched from a push notification
Action :	Action name to be launched by tapping the notification


[Setting example]

<CSipSiple>
Activity :	com.csipsimple.ui.SipHome
Action :	com.csipsimple.phone.action.CALLLOG

<Zoiper>
Activity :	com.zoiper.android.ui.SplashScreen
Action :	(blank)


[Sample file]

<cURL>
gcm.bat :	Windows 用プッシュ発信スクリプト
gcm.sh :	Linux 用プッシュ発信スクリプト

gcm.bat :	Push script for Windows
gcm.sh :	Push script for Linux

<Asterisk>
extensions.conf :	ダイヤルプラン
queues.conf:		コールキュー

extensions.conf :	Dialplan
queues.conf:		call queues
