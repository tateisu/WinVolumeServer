#nullable enable
#pragma warning disable IDE1006

using System;
using System.Configuration;
using System.IO;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace WinVolumeServer {
    public static class JsonExt {
        public static void saveTo(this JObject src, string path) {
            try {
                var jsonText = JsonConvert.SerializeObject( src );
                File.WriteAllText( path, jsonText, Encoding.UTF8 );
            } catch (Exception ex) {
                Console.WriteLine( $"json save failed. {path}, {ex}" );
            }
        }
        public static JObject loadJson(this string path) {
            try {
                var jsonText = File.ReadAllText( path, Encoding.UTF8 );
                var a = (JObject?)JsonConvert.DeserializeObject( jsonText );
                if (a != null) {
                    return a;
                }
                throw new NullReferenceException( "DeserializeObject returns null." );
            } catch (Exception ex) {
                Console.WriteLine( $"json load failed. {path} {ex}" );
                return new JObject();
            }
        }
    }

    class PrefMeta {
        private readonly String key;
        private readonly String defVal;

        public PrefMeta(String key, String defVal) {
            this.key = key;
            this.defVal = defVal;
        }

        internal void set(JObject src, String value, String filePath) {
            src[ key ] = value;
            src.saveTo( filePath );
        }

        internal String get(JObject src) {
            var token = src[ key ];
            if (token != null && token.Type.Equals( JTokenType.String )) {
                return (string?)token ?? defVal;
            } else {
                return defVal;
            }
        }
    }

    public class Pref {
        private static String settingFileName() {
            var appSettings = ConfigurationManager.OpenExeConfiguration(
                    ConfigurationUserLevel.PerUserRoamingAndLocal
            );
            var dir = Path.GetDirectoryName( appSettings.FilePath );
            Directory.CreateDirectory( dir );
            return Path.Combine( dir, "appSetting.json" );
        }

        private readonly String filePath = settingFileName();
        private JObject? json = null;

        JObject ensureLoad() {
            var json = this.json;
            if (json == null) {
                json = filePath.loadJson();
                this.json = json;
            }
            return json;
        }

        private static readonly PrefMeta serverPortMeta = new PrefMeta( "serverPort", "2021" );
        public string serverPort {
            set => serverPortMeta.set( ensureLoad(), value, filePath );
            get => serverPortMeta.get( ensureLoad() );
        }

        private static readonly PrefMeta serverErrorMeta = new PrefMeta( "serverError", "" );
        public string serverError {
            set => serverErrorMeta.set( ensureLoad(), value, filePath );
            get => serverErrorMeta.get( ensureLoad() );
        }

        private static readonly PrefMeta passwordMeta = new PrefMeta( "password", "" );
        public string password {
            set => passwordMeta.set( ensureLoad(), value, filePath );
            get => passwordMeta.get( ensureLoad() );
        }

        private static readonly PrefMeta voiceMeeterGainMeta = new PrefMeta( "voiceMeeterGain", "" );
        public string voiceMeeterGain {
            set => voiceMeeterGainMeta.set( ensureLoad(), value, filePath );
            get => voiceMeeterGainMeta.get( ensureLoad() );
        }
    }
}
