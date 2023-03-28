#nullable enable
#pragma warning disable IDE1006

using System.Configuration;

namespace WinVolumeServer {
    public class Pref : ApplicationSettingsBase {

        private const string KEY_SERVER_PREFIX = "serverPrefix";
        private const string KEY_SERVER_ERROR = "serverError";
        private const string KEY_PASSWORD = "password";
        private const string KEY_VOICE_MEETER_GAIN = "voiceMeeterGain";

        [UserScopedSetting()]
        [DefaultSettingValue("")]
        public string serverError {
            get { return (string)this[KEY_SERVER_ERROR]; }
            set { this[KEY_SERVER_ERROR] = value; }
        }

        [UserScopedSetting()]
        [DefaultSettingValue("2021")]
        public string serverPrefix {
            get { return (string)this[KEY_SERVER_PREFIX]; }
            set { this[KEY_SERVER_PREFIX] = value; }
        }

        [UserScopedSetting()]
        [DefaultSettingValue("")]
        public string password {
            get { return (string)this[KEY_PASSWORD]; }
            set { this[KEY_PASSWORD] = value; }
        }

        [UserScopedSetting()]
        [DefaultSettingValue( "" )]
        public string voiceMeeterGain {
            get {
                return (string)this[ KEY_VOICE_MEETER_GAIN ];
            }
            set {
                this[ KEY_VOICE_MEETER_GAIN ] = value;
            }
        }

    }
}
