#nullable enable
#pragma warning disable IDE1006

using System.Windows.Forms;

namespace WinVolumeServer {
    class Hub {
        public static readonly Pref pref = new Pref();
        public static readonly MyHttpServer httpServer = new MyHttpServer();
        public static readonly NotifyIconHolder iconHolder = new NotifyIconHolder();
        public static readonly VoiceMeeterClient voiceMeeter = new VoiceMeeterClient();

        public static Form1? form1;

        internal static void startServer() => 
            httpServer.start( pref.serverPort);

        private static void closeForm() {
            form1?.Close();
            form1?.Dispose();
            form1 = null;
        }

        public static void onProgramStart() {
            iconHolder.start();
            Hub.startServer();
        }

        public static void onMenuExit() {
            closeForm();
            iconHolder.stop();
            httpServer.stop();
            voiceMeeter.Dispose();
            Application.Exit();
        }

        public static void onMenuConfig() {
            closeForm();
            form1 = new Form1 {
                Visible = true
            };
        }
    }
}
