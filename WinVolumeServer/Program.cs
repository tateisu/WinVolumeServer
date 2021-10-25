#nullable enable
#pragma warning disable IDE1006

using System;
using System.Net;
using System.Windows.Forms;

namespace WinVolumeServer {
    static class Program {

        [MTAThread]
        static void Main() {

            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            // タスクトレイ常駐アプリなので、起動時にはフォームを開かない
            // Application.Run(new Form1());

            Hub.onProgramStart();

            // Formを表示しないで実行する
            Application.Run();
        }
    }
}
