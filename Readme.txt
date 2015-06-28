[Overview]

任意のアプリをプッシュにより起動する。


[Preparation]

下記のサイトでプロジェクト番号と API キーを取得する。
https://console.developers.google.com/


[Settings]

Project No :	上記のプロジェクト番号。（必須）
Server URL :	Registration ID を受信するサーバーの URL。
Package :	プッシュで起動するアプリのパッケージ名。
Activity :	プッシュで起動するアクティビティ名。
Notification :	プッシュ通知をタップした時に起動するアクティビティ名。
Monitor :	終了を監視するアクティビティ名。このアクティビティが終了した時に端末をスリープにする。


[Setting examples]

- Lollipop -

<CSipSiple>
Package :	com.csipsimple
Activity :	com.csipsimple.ui.SipHome
Notification :	com.csipsimple.phone.action.CALLLOG
Monitor :	(blank)

<Zoiper>
Package :	com.zoiper.android.app
Activity :	com.zoiper.android.ui.SplashScreen
Notification :	com.zoiper.android.ui.SplashScreen
Monitor :	(blank)

- Other than Lollipop -

<CSipSiple>
Package :	com.csipsimple
Activity :	com.csipsimple.ui.SipHome
Notification :	com.csipsimple.phone.action.CALLLOG
Monitor :	com.csipsimple.ui.incall.InCallActivity

<Zoiper>
Package :	com.zoiper.android.app
Activity :	com.zoiper.android.ui.SplashScreen
Notification :	com.zoiper.android.ui.SplashScreen
Monitor :	com.zoiper.android.ui.InCallScreen


[Sample files]

<cURL>
gcm.bat :		Windows 用プッシュ発信スクリプト
gcm.sh :		Linux 用プッシュ発信スクリプト

<Asterisk>
extensions.conf :	ダイヤルプラン
extensions2.conf :	固定回線共用ダイヤルプラン
