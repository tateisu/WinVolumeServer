
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
            this.tbServerPort = new System.Windows.Forms.TextBox();
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
            this.label2 = new System.Windows.Forms.Label();
            this.tbVoiceMeeterGain = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // labelServer
            // 
            resources.ApplyResources(this.labelServer, "labelServer");
            this.labelServer.Name = "labelServer";
            // 
            // tbServerPort
            // 
            resources.ApplyResources(this.tbServerPort, "tbServerPort");
            this.tbServerPort.Name = "tbServerPort";
            this.tbServerPort.TextChanged += new System.EventHandler(this.tbServerPrefix_TextChanged);
            // 
            // tbServerError
            // 
            resources.ApplyResources(this.tbServerError, "tbServerError");
            this.tbServerError.ForeColor = System.Drawing.SystemColors.WindowText;
            this.tbServerError.Name = "tbServerError";
            this.tbServerError.ReadOnly = true;
            // 
            // btnServerRestart
            // 
            resources.ApplyResources(this.btnServerRestart, "btnServerRestart");
            this.btnServerRestart.Name = "btnServerRestart";
            this.btnServerRestart.UseVisualStyleBackColor = true;
            this.btnServerRestart.Click += new System.EventHandler(this.btnServerRestart_Click);
            // 
            // labelAudioDevice
            // 
            resources.ApplyResources(this.labelAudioDevice, "labelAudioDevice");
            this.labelAudioDevice.Name = "labelAudioDevice";
            // 
            // tbAudioDevice
            // 
            resources.ApplyResources(this.tbAudioDevice, "tbAudioDevice");
            this.tbAudioDevice.Name = "tbAudioDevice";
            this.tbAudioDevice.ReadOnly = true;
            // 
            // btnAudioDeviceCheck
            // 
            resources.ApplyResources(this.btnAudioDeviceCheck, "btnAudioDeviceCheck");
            this.btnAudioDeviceCheck.Name = "btnAudioDeviceCheck";
            this.btnAudioDeviceCheck.UseVisualStyleBackColor = true;
            this.btnAudioDeviceCheck.Click += new System.EventHandler(this.btnAudioDeviceCheck_Click);
            // 
            // labelVolume
            // 
            resources.ApplyResources(this.labelVolume, "labelVolume");
            this.labelVolume.Name = "labelVolume";
            // 
            // tbVolume
            // 
            resources.ApplyResources(this.tbVolume, "tbVolume");
            this.tbVolume.Name = "tbVolume";
            // 
            // btnVolume
            // 
            resources.ApplyResources(this.btnVolume, "btnVolume");
            this.btnVolume.Name = "btnVolume";
            this.btnVolume.UseVisualStyleBackColor = true;
            this.btnVolume.Click += new System.EventHandler(this.btnVolume_Click);
            // 
            // label1
            // 
            resources.ApplyResources(this.label1, "label1");
            this.label1.Name = "label1";
            // 
            // tbPassword
            // 
            resources.ApplyResources(this.tbPassword, "tbPassword");
            this.tbPassword.Name = "tbPassword";
            this.tbPassword.TextChanged += new System.EventHandler(this.tbPassword_TextChanged);
            // 
            // label2
            // 
            resources.ApplyResources(this.label2, "label2");
            this.label2.Name = "label2";
            // 
            // tbVoiceMeeterGain
            // 
            resources.ApplyResources(this.tbVoiceMeeterGain, "tbVoiceMeeterGain");
            this.tbVoiceMeeterGain.Name = "tbVoiceMeeterGain";
            this.tbVoiceMeeterGain.TextChanged += new System.EventHandler(this.tbVoiceMeeterGain_TextChanged);
            // 
            // Form1
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.tbVoiceMeeterGain);
            this.Controls.Add(this.label2);
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
            this.Controls.Add(this.tbServerPort);
            this.Controls.Add(this.labelServer);
            this.MaximizeBox = false;
            this.Name = "Form1";
            this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Show;
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label labelServer;
        private System.Windows.Forms.TextBox tbServerPort;
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
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox tbVoiceMeeterGain;
    }
}

