# WinVolumeServer

PCの音量をHTTP経由で制御したくなったので書いた。

----
## WinVolumeServer app
外部からHTTPリクエストを受け取ってPCの現在の出力デバイスのマスター音量を変更できるアプリ。

- タスクトレイに常駐する
- 右クリック→設定
- HTTPサーバとして動作する

![image](https://user-images.githubusercontent.com/333944/138663828-4a92c18d-b23c-46e9-9fd4-e52702892cd5.png)

----
## WinVolumeclient app (for Android)
WinVolumeServerにアクセスしてリモートで音量を制御するアプリ。

![image](https://user-images.githubusercontent.com/333944/138664181-9885df66-c4b2-4e23-a4f7-3d4948b612af.png)

----
## API

### 認証
パスワードがカラではないなら、以下のヘッダをセットすること

|ヘッダ|値|
|--|--|
|X-Password-Time|リクエストを投げた際の時刻(unixtime,ミリ秒単位)|
|X-Password-Digest|"{時刻}:{パスワード}"をUTF-8にしてSHA256ダイジェストにしてBase64(url safe)エンコードした文字列|

### GET /volume

レスポンス：以下のデータを持つjsonObjectを返す。

|キー|型|説明|
|--|--|--|
|device|string?|現在の出力デバイスの名前。空文字列やnullがありうる|
|volume|float?|現在のボリューム。db単位、範囲は-96…0|

### POST /volume?v=X
ボリュームを設定する。
- クエリパラメータvにボリュームをdb単位で指定する。
- 出力デバイスによって指定可能な範囲が代わり、自動的にクリップされる。

レスポンス：設定後の状態を 返す。形式は`GET /volume` と同じ。
