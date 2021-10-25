#nullable enable
#pragma warning disable IDE1006

using System;
using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Windows.Forms;

namespace WinVolumeServer {
    class Hub {
        public static readonly Pref pref = new Pref();
        public static readonly MyHttpServer httpServer = new MyHttpServer();
        public static readonly NotifyIconHolder iconHolder = new NotifyIconHolder();
        public static Form1? form1;

        internal static void startServer() {
            httpServer.start(pref.serverPrefix);
        }

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
            Application.Exit();
        }

        public static void onMenuConfig() {
            closeForm();
            var form = new Form1();
            form.Visible = true;
            form1 = form;
        }
    }
}
