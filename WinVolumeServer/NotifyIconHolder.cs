#nullable enable
#pragma warning disable IDE1006

using System;
using System.Diagnostics;
using System.Windows.Forms;

namespace WinVolumeServer {

    class NotifyIconHolder {

        private static ToolStripMenuItem createToolStripMenuItem(String text, EventHandler handler) {
            var toolStripMenuItem = new ToolStripMenuItem(text);
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
                var contextMenuStrip = new ContextMenuStrip();
                contextMenuStrip.Items.Add(createToolStripMenuItem("&Settings", (sender, e) => Hub.onMenuConfig()));
                contextMenuStrip.Items.Add(createToolStripMenuItem("&Exit", (sender, e) => Hub.onMenuExit()));
                notifyIcon.ContextMenuStrip = contextMenuStrip;
            } catch (Exception ex) {
                Debug.WriteLine(ex);
            }
        }
    }
}
