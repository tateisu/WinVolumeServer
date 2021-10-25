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

