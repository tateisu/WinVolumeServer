#nullable enable
#pragma warning disable IDE1006

using System;
using System.Diagnostics;
using System.Windows.Forms;

namespace WinVolumeServer {

    class NotifyIconHolder {

        private static ToolStripMenuItem createToolStripMenuItem(String text, EventHandler handler) {
            ToolStripMenuItem toolStripMenuItem = new ToolStripMenuItem(text);
            toolStripMenuItem.Click += handler;
            return toolStripMenuItem;
        }

        NotifyIcon? notifyIcon;

        public void stop() {
            var ni = this.notifyIcon;
            if (ni != null) {
                ni.Icon = null;
                ni.Dispose();
                Application.DoEvents();
            }
            this.notifyIcon = null;
        }

        public void start() {
            try {
                stop();

                var notifyIcon = new NotifyIcon {
                    Icon = Properties.Resources.main,
                    Visible = true,
                    Text = "WinVolumeServer"
                };

                this.notifyIcon = notifyIcon;

                // コンテキストメニュー
                ContextMenuStrip contextMenuStrip = new ContextMenuStrip();
                contextMenuStrip.Items.Add(createToolStripMenuItem("&設定", (sender, e) => Hub.onMenuConfig()));
                contextMenuStrip.Items.Add(createToolStripMenuItem("&終了", (sender, e) => Hub.onMenuExit()));
                notifyIcon.ContextMenuStrip = contextMenuStrip;
            } catch (Exception ex) {
                Debug.WriteLine(ex);
            }
        }
    }
}
