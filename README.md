# WinVolumeServer

PCの音量をHTTP経由で制御したくなったので書いた。

----
## WinVolumeServer app
外部からHTTPリクエストを受け取ってPCの現在の出力デバイスのマスター音量を変更できるアプリ。

### インストール手順
- リリースページ https://github.com/tateisu/WinVolumeServer/releases から適当なバージョンのリリースを見る
- Assets にある `WinVolumeServer-{日付}-{時刻}.zip` をダウンロード
- PCの適当なフォルダに解凍する
- (任意)セキュリティソフトでスキャンする。俺を信用するな。

### 起動手順
- 解凍したフォルダの中の WinVolumeServer.exe を実行する
- HTTPサーバが動作するからファイアウォールに何か言われるけど、プライベートネットワークに対して許可する
- タスクトレイに常駐する
- 右クリック→設定で設定画面を開く
- 右クリック→終了でアプリを終了する

![image](https://user-images.githubusercontent.com/333944/138663828-4a92c18d-b23c-46e9-9fd4-e52702892cd5.png)

----
## WinVolumeclient app (for Android)
WinVolumeServerにアクセスしてリモートで音量を制御するアプリ。

### インストール手順
- リリースページ https://github.com/tateisu/WinVolumeServer/releases から適当なバージョンのリリースを見る
- Assets にある `WinVolumeServer-{日付}-{時刻}.zip` をダウンロード
- (任意)セキュリティソフトでスキャンする。俺を信用するな。
- zipの中にある WinVolumeClient.apk をAndroid端末にコピーして、ファイルアプリなどで開く
- インストール権限がどうとか言われるので、ファイルアプリ等にインストール権限を与える
- 大丈夫そうならもう一度ファイルアプリなどで開くとインストールするか尋ねられる
- 後は画面の指示に従う

### 起動手順
- VolumeClient アプリを開く
- 「接続設定の表示」をオンにするとサーバのアドレスとポート番号とパスワードが出てくる
- PCのWinVolumeServerの設定画面を見ながら入力する
- 正しければ「接続した。」が表示されて操作できる。

![image](https://user-images.githubusercontent.com/333944/138828874-76e4aefb-ec14-4890-95b7-c3baef87b110.png)

----
## VoiceMeeter 連動

以下の条件を満たすとVoiceMeeterのスライダのdB値を直接変更する
- サーバ側アプリがVoiceMeeterのインストール先を検出できる。
- サーバ側アプリの「現在のオーディオ出力」の１行目に「VoiceMeeter」という単語が含まれる。
- サーバ側アプリの「VoiceMeeterのGainプロパティ」にスライダのプロパティ名が指定されている。

### 「VoiceMeeterのGainプロパティ」に指定するプロパティ名の調べ方
- VB側で目的の音量スライダを適当に動かして、他のスライダと区別できる数字にする。
- WinVolumeServerのサーバ側アプリの「現在のオーディオ出力」の右上の「再取得」を押す。
- 出力の内容に、その数字が出る行があるはず。
- その行の左端の部分を「VoiceMeeterのGainプロパティ」に指定する。
- 例：「Bus[0].Gain // -43 // Bus A1」の左端の「Bus[0].Gain」。

### 補足
- プロパティ名の詳細は https://github.com/vburel2018/Voicemeeter-SDK にあるPDFファイルを参照。
- VBのスライダは下限-60dBと決まっているので、それより低い値を指定しても-60にクリップされる。

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
- 出力デバイスによって指定可能な範囲は異なる。自動的にクリップされる。

レスポンス：設定後の状態を 返す。形式は`GET /volume` と同じ。
