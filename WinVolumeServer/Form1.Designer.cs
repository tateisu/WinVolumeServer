
namespace WinVolumeServer
{
    partial class Form1
    {
        /// <summary>
        /// 必要なデザイナー変数です。
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 使用中のリソースをすべてクリーンアップします。
        /// </summary>
        /// <param name="disposing">マネージド リソースを破棄する場合は true を指定し、その他の場合は false を指定します。</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows フォーム デザイナーで生成されたコード

        /// <summary>
        /// デザイナー サポートに必要なメソッドです。このメソッドの内容を
        /// コード エディターで変更しないでください。
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            this.labelServer = new System.Windows.Forms.Label();
            this.tbServerPrefix = new System.Windows.Forms.TextBox();
            this.tbServerError = new System.Windows.Forms.TextBox();
            this.btnServerRestart = new System.Windows.Forms.Button();
            this.labelAudioDevice = new System.Windows.Forms.Label();
            this.tbAudioDevice = new System.Windows.Forms.TextBox();
            this.btnAudioDeviceCheck = new System.Windows.Forms.Button();
            this.labelVolume = new System.Windows.Forms.Label();
            this.tbVolume = new System.Windows.Forms.TextBox();
            this.btnVolume = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.tbPassword = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // labelServer
            // 
            this.labelServer.Location = new System.Drawing.Point(14, 15);
            this.labelServer.Name = "labelServer";
            this.labelServer.Size = new System.Drawing.Size(95, 16);
            this.labelServer.TabIndex = 0;
            this.labelServer.Text = "待機ポート番号";
            this.labelServer.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // tbServerPrefix
            // 
            this.tbServerPrefix.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tbServerPrefix.Location = new System.Drawing.Point(115, 14);
            this.tbServerPrefix.Name = "tbServerPrefix";
            this.tbServerPrefix.Size = new System.Drawing.Size(205, 19);
            this.tbServerPrefix.TabIndex = 1;
            this.tbServerPrefix.TextChanged += new System.EventHandler(this.tbServerPrefix_TextChanged);
            // 
            // tbServerError
            // 
            this.tbServerError.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tbServerError.ForeColor = System.Drawing.Color.Red;
            this.tbServerError.Location = new System.Drawing.Point(16, 37);
            this.tbServerError.Multiline = true;
            this.tbServerError.Name = "tbServerError";
            this.tbServerError.ReadOnly = true;
            this.tbServerError.ScrollBars = System.Windows.Forms.ScrollBars.Both;
            this.tbServerError.Size = new System.Drawing.Size(374, 90);
            this.tbServerError.TabIndex = 2;
            // 
            // btnServerRestart
            // 
            this.btnServerRestart.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btnServerRestart.AutoSize = true;
            this.btnServerRestart.Location = new System.Drawing.Point(326, 12);
            this.btnServerRestart.Name = "btnServerRestart";
            this.btnServerRestart.Size = new System.Drawing.Size(64, 22);
            this.btnServerRestart.TabIndex = 3;
            this.btnServerRestart.Text = "再起動";
            this.btnServerRestart.UseVisualStyleBackColor = true;
            this.btnServerRestart.Click += new System.EventHandler(this.btnServerRestart_Click);
            // 
            // labelAudioDevice
            // 
            this.labelAudioDevice.Location = new System.Drawing.Point(14, 164);
            this.labelAudioDevice.Name = "labelAudioDevice";
            this.labelAudioDevice.Size = new System.Drawing.Size(118, 25);
            this.labelAudioDevice.TabIndex = 4;
            this.labelAudioDevice.Text = "現在のオーディオ出力";
            this.labelAudioDevice.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // tbAudioDevice
            // 
            this.tbAudioDevice.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tbAudioDevice.Location = new System.Drawing.Point(16, 192);
            this.tbAudioDevice.Multiline = true;
            this.tbAudioDevice.Name = "tbAudioDevice";
            this.tbAudioDevice.ReadOnly = true;
            this.tbAudioDevice.ScrollBars = System.Windows.Forms.ScrollBars.Both;
            this.tbAudioDevice.Size = new System.Drawing.Size(374, 54);
            this.tbAudioDevice.TabIndex = 5;
            // 
            // btnAudioDeviceCheck
            // 
            this.btnAudioDeviceCheck.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btnAudioDeviceCheck.Location = new System.Drawing.Point(326, 164);
            this.btnAudioDeviceCheck.Name = "btnAudioDeviceCheck";
            this.btnAudioDeviceCheck.Size = new System.Drawing.Size(64, 25);
            this.btnAudioDeviceCheck.TabIndex = 6;
            this.btnAudioDeviceCheck.Text = "再取得";
            this.btnAudioDeviceCheck.UseVisualStyleBackColor = true;
            this.btnAudioDeviceCheck.Click += new System.EventHandler(this.btnAudioDeviceCheck_Click);
            // 
            // labelVolume
            // 
            this.labelVolume.Location = new System.Drawing.Point(14, 257);
            this.labelVolume.Name = "labelVolume";
            this.labelVolume.Size = new System.Drawing.Size(95, 19);
            this.labelVolume.TabIndex = 7;
            this.labelVolume.Text = "音量(dB。-96…0)";
            this.labelVolume.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // tbVolume
            // 
            this.tbVolume.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tbVolume.Location = new System.Drawing.Point(115, 257);
            this.tbVolume.Name = "tbVolume";
            this.tbVolume.Size = new System.Drawing.Size(216, 19);
            this.tbVolume.TabIndex = 8;
            // 
            // btnVolume
            // 
            this.btnVolume.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btnVolume.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.btnVolume.Location = new System.Drawing.Point(337, 252);
            this.btnVolume.Name = "btnVolume";
            this.btnVolume.Size = new System.Drawing.Size(53, 28);
            this.btnVolume.TabIndex = 9;
            this.btnVolume.Text = "設定";
            this.btnVolume.UseVisualStyleBackColor = true;
            this.btnVolume.Click += new System.EventHandler(this.btnVolume_Click);
            // 
            // label1
            // 
            this.label1.Location = new System.Drawing.Point(14, 133);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(59, 19);
            this.label1.TabIndex = 10;
            this.label1.Text = "パスワード";
            this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // tbPassword
            // 
            this.tbPassword.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tbPassword.Location = new System.Drawing.Point(79, 133);
            this.tbPassword.Name = "tbPassword";
            this.tbPassword.Size = new System.Drawing.Size(311, 19);
            this.tbPassword.TabIndex = 11;
            this.tbPassword.TextChanged += new System.EventHandler(this.tbPassword_TextChanged);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(402, 301);
            this.Controls.Add(this.tbPassword);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btnVolume);
            this.Controls.Add(this.tbVolume);
            this.Controls.Add(this.labelVolume);
            this.Controls.Add(this.btnAudioDeviceCheck);
            this.Controls.Add(this.tbAudioDevice);
            this.Controls.Add(this.labelAudioDevice);
            this.Controls.Add(this.btnServerRestart);
            this.Controls.Add(this.tbServerError);
            this.Controls.Add(this.tbServerPrefix);
            this.Controls.Add(this.labelServer);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.Name = "Form1";
            this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Show;
            this.Text = "WinVolumeServer";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label labelServer;
        private System.Windows.Forms.TextBox tbServerPrefix;
        private System.Windows.Forms.TextBox tbServerError;
        private System.Windows.Forms.Button btnServerRestart;
        private System.Windows.Forms.Label labelAudioDevice;
        private System.Windows.Forms.TextBox tbAudioDevice;
        private System.Windows.Forms.Button btnAudioDeviceCheck;
        private System.Windows.Forms.Label labelVolume;
        private System.Windows.Forms.TextBox tbVolume;
        private System.Windows.Forms.Button btnVolume;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox tbPassword;
    }
}

