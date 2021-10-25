#nullable enable
#pragma warning disable IDE1006

using System;
using System.Windows.Forms;

namespace WinVolumeServer {
    public partial class Form1 : Form {
        public Form1() {
            // タスクバーに表示しない
            this.ShowInTaskbar = false;
            InitializeComponent();
            showContents();
        }

        public void showContents() {
            if (InvokeRequired) {
                Invoke(new MethodInvoker(showContents));
                return;
            }
            tbServerPrefix.Text = Hub.pref.serverPrefix;
            tbServerError.Text = Hub.pref.serverError;
            tbAudioDevice.Text = Volume.getDeviceName();
            double? dv = Volume.getVolume();
            tbVolume.Text = dv == null ? "" : dv.ToString();
        }

        private void btnServerRestart_Click(object sender, System.EventArgs e) {
            Hub.startServer();
            showContents();
        }

        private void tbServerPrefix_TextChanged(object sender, System.EventArgs e) {
            var text = tbServerPrefix.Text.Trim();
            if (text == Hub.pref.serverPrefix) return;
            Hub.pref.serverPrefix = text;
            Hub.pref.Save();
        }

        private void tbPassword_TextChanged(object sender, EventArgs e) {
            var text = tbPassword.Text.Trim();
            if (text == Hub.pref.password) return;
            Hub.pref.password = text;
            Hub.pref.Save();
        }

        private void btnAudioDeviceCheck_Click(object sender, System.EventArgs e) {
            showContents();
        }

        private void btnVolume_Click(object sender, EventArgs e) {
            if (!float.TryParse(tbVolume.Text, out float volumeFloat)) {
                MessageBox.Show(
                    "入力内容を数値として解釈できません",
                    "エラー",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
                return;
            }
            try {
                Volume.setVolume(volumeFloat);
            } catch (Exception ex) {
                MessageBox.Show(
                    $"ボリュームの設定に失敗しました {ex.GetType().Name} {ex.Message}",
                    "エラー",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
            showContents();
        }
    }
}
