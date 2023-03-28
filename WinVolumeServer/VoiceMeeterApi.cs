#nullable enable
#pragma warning disable IDE1006
#pragma warning disable IDE0007 // 暗黙的な型の使用

using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;

namespace WinVolumeServer {

    public class VolumeSlot {
        public String name;
        public String propGain;
        public String? propDeviceName;
        public VolumeSlot(String name, String propGain) {
            this.name = name;
            this.propGain = propGain;
        }
    }
    
    internal class ServerType {
        public Int32 typeId;
        public String name;
        internal VolumeSlot[] volumeSlots;
        internal ServerType(Int32 typeId, String name, params VolumeSlot[] volumeSlots) {
            this.typeId = typeId;
            this.name = name;
            this.volumeSlots = volumeSlots;
        }

        public static ServerType[] serverTypes = new ServerType[] {
            new ServerType(
                1 ,
                "Voicemeeter",
                new VolumeSlot("Strip 1","Strip[0].Gain"),
                new VolumeSlot("Strip 2","Strip[1].Gain"),
                new VolumeSlot("Virtual Input","Strip[2].Gain"),
                new VolumeSlot("Bus A","Bus[0].Gain"),
                new VolumeSlot("Bus B","Bus[1].Gain")
            ),
            new ServerType(
                2 ,
                "Voicemeeter Banana",
                new VolumeSlot("Strip 1","Strip[0].Gain"),
                new VolumeSlot("Strip 2","Strip[1].Gain"),
                new VolumeSlot("Strip 3","Strip[2].Gain"),
                new VolumeSlot("Virtual Input","Strip[3].Gain"),
                new VolumeSlot("Virtual AUX","Strip[4].Gain"),
                new VolumeSlot("Bus A1","Bus[0].Gain"),
                new VolumeSlot("Bus A2","Bus[1].Gain"),
                new VolumeSlot("Bus A3","Bus[2].Gain"),
                new VolumeSlot("Bus B1","Bus[3].Gain"),
                new VolumeSlot("Bus B2","Bus[4].Gain")
            ),
            new ServerType(
                3,
                "Potato",
                new VolumeSlot("Strip 1","Strip[0].Gain"),
                new VolumeSlot("Strip 2","Strip[1].Gain"),
                new VolumeSlot("Strip 3","Strip[2].Gain"),
                new VolumeSlot("Strip 4","Strip[3].Gain"),
                new VolumeSlot("Strip 5","Strip[4].Gain"),
                new VolumeSlot("Virtual Input","Strip[5].Gain"),
                new VolumeSlot("Virtual AUX","Strip[6].Gain"),
                new VolumeSlot("Virtual VAIO3","Strip[7].Gain"),
                new VolumeSlot("Bus A1","Bus[0].Gain"),
                new VolumeSlot("Bus A2","Bus[1].Gain"),
                new VolumeSlot("Bus A3","Bus[2].Gain"),
                new VolumeSlot("Bus A4","Bus[3].Gain"),
                new VolumeSlot("Bus A5","Bus[4].Gain"),
                new VolumeSlot("Bus B1","Bus[5].Gain"),
                new VolumeSlot("Bus B2","Bus[6].Gain"),
                new VolumeSlot("Bus B3","Bus[7].Gain")
            ),
        };
    }

    public enum VoiceMeeterLoginResponse {
        OK = 0,
        OkVoicemeeterNotRunning = 1,
        NoClient = -1,
        AlreadyLoggedIn = -2,
    }
    public enum VoiceMeeterRunResponse {
        OK = 0,
        NotInstalled= -1,
        UnknownVType = -2,
    }

    public static class VoiceMeeterApi {

        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_Login" )]
        public static extern VoiceMeeterLoginResponse Login();

        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_Logout" )]
        public static extern VoiceMeeterLoginResponse Logout();

        //  vType : Voicemeeter type  (1 = Voicemeeter, 2= Voicemeeter Banana, 3= Voicemeeter Potato, 6 = Potato x64 bits).
        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_RunVoicemeeter" )]
        public static extern VoiceMeeterRunResponse RunVoiceMeeter(Int32 vType);

        //  vType : Voicemeeter type  (1 = Voicemeeter, 2= Voicemeeter Banana, 3= Voicemeeter Potato, 6 = Potato x64 bits).
        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_GetVoicemeeterType" )]
        public static extern Int32 GetVoicemeeterType(ref Int32 pType);


        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_SetParameterFloat" )]
        public static extern Int32 SetParameterFloat(String szParamName, Single value);

        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_GetParameterFloat" )]
        public static extern Int32 GetParameterFloat(String szParamName, ref Single value);

        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_GetParameterStringW", CharSet = CharSet.Unicode )]
        public static extern Int32 GetParameterString(String szParamName, StringBuilder s);

        [DllImport( "VoicemeeterRemote.dll", EntryPoint = "VBVMR_IsParametersDirty" )]
        public static extern Int32 IsParametersDirty();

        [DllImport( "kernel32.dll" )]
        private static extern IntPtr LoadLibrary(String dllToLoad);

        private static IntPtr? _dllHandle;

        public static Boolean IsDllLoaded =>
            _dllHandle.HasValue;

        public static void LoadDll(String dllPath) {
            if (!_dllHandle.HasValue) {
                _dllHandle = LoadLibrary( dllPath );
            }
        }

        private const String regKey = @"HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall";
        private const String uninstKey = "VB:Voicemeeter {17359A74-1236-5467}";

        public static String? GetVoicemeeterDir() {
            var key = $"{regKey}\\{uninstKey}";
            var k = Registry.GetValue( key, "UninstallString", null );
            return k == null ? null : System.IO.Path.GetDirectoryName( k.ToString() );
        }
    }

    public class VoiceMeeterClient : IDisposable {
        // Dispose時にログアウトAPIを呼び出す
        private Boolean IsLogin = false;

        // VoiceMeeterは状態取得しても少し古い情報を返すことがあるので、
        // セットした直後はキャッシュした値を返す
        private Single lastVolume = -96f;
        private Int64 lastVolumeAt = 0;

        private Int64 Now =>
            DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

        // VoiceMeeterのAPIを利用可能なら真
        private Boolean ensureLogin() {
            try {
                // まだロードしてないならDLLをロード
                if (!VoiceMeeterApi.IsDllLoaded) {
                    var vmDir = VoiceMeeterApi.GetVoicemeeterDir();
                    if (vmDir == null) {
                        Console.WriteLine( "GetVoicemeeterDir returns null." );
                        return false;
                    }
                    var dllPath = System.IO.Path.Combine( vmDir, "VoicemeeterRemote.dll" );
                    try {
                        VoiceMeeterApi.LoadDll( dllPath );
                        Console.WriteLine( "VoiceMeeterApi.LoadDll ok." );
                    } catch (Exception ex) {
                        Console.WriteLine( $"VoiceMeeterApi.LoadDll failed. dllPath={dllPath},ex={ex}" );
                        return false;
                    }
                }

                // ログインAPIを呼び出す
                var lr = VoiceMeeterApi.Login();

                switch (lr) {
                case VoiceMeeterLoginResponse.OK:
                case VoiceMeeterLoginResponse.AlreadyLoggedIn:
                    IsLogin = true;
                    return true;
                default:
                    Console.WriteLine( $"VoiceMeeterClient: LoginResponse={lr}" );
                    return false;
                }
            } catch(Exception ex) {
                Console.WriteLine( $"VoiceMeeterClient.ensureLogin failed. {ex}" );
                return false;
            }
        }

        ~VoiceMeeterClient() {
            Dispose( false );
        }

        public void Dispose() {
            Dispose( true );
            GC.SuppressFinalize( this );
        }

        protected virtual void Dispose(Boolean disposing) {
            if (IsLogin) {
                Console.WriteLine( $"VoiceMeeterApi.Logout" );
                VoiceMeeterApi.Logout();
                IsLogin = false;
            }
        }

        public Single? GetParameterFloat(String n) {
            if (!ensureLogin()) return null;
            Single output = -1f;
            VoiceMeeterApi.GetParameterFloat( n, ref output );
            return output;
        }

        public void SetParameterFloat(String n, Single v) => VoiceMeeterApi.SetParameterFloat( n, v );

        public Boolean Poll() => VoiceMeeterApi.IsParametersDirty() == 1;

        public String deviceInfo() {
            var strList = new List<String>();
            if (ensureLogin()) {
                Poll();
                Int32 type = 0;
                var rv = VoiceMeeterApi.GetVoicemeeterType( ref type );
                if (rv != 0) {
                    Console.WriteLine( $"GetVoicemeeterType failed. {rv}" );
                } else {
                    var serverType = Array.Find( ServerType.serverTypes, it => it.typeId == type );
                    if (serverType == null) {
                        Console.WriteLine( $"GetVoicemeeterType unknown type: {type}" );
                    } else {
                        var sb = new StringBuilder( 512 + 1 );
                        strList.Add( serverType.name );
                        foreach (var slot in serverType.volumeSlots) {
                            Single gain = Single.NaN;
                            VoiceMeeterApi.GetParameterFloat( slot.propGain, ref gain );
                            if (Single.IsNaN( gain )) {
                                continue;
                            }
                            strList.Add( $"{slot.propGain} // {gain} // {slot.name}" );
                        }
                    }
                }
            }
            return String.Join( "\r\n", strList );
        }

        public Single? getVolume() {
            if (Now - lastVolumeAt < 100) {
                return lastVolume;
            }

            var propGain = Hub.pref.voiceMeeterGain;
            if (propGain == null || propGain.Length == 0) {
                return null;
            }else if (!ensureLogin()) {
                return null;
            }
            Poll();
            Single gain = Single.NaN;
            VoiceMeeterApi.GetParameterFloat( propGain, ref gain );
            if (Single.IsNaN( gain )) {
                return null;
            }
            return   gain;
        }

        public Boolean setVolume(Single newVolume) {
            var propGain = Hub.pref.voiceMeeterGain;
            if (propGain == null || propGain.Length == 0) {
                Console.WriteLine( "setVolume: missing propGain." );
                return false;
            }else if(!ensureLogin()) {
                Console.WriteLine( "setVolume: login failed." );
                return false;
            }
            if (0 != VoiceMeeterApi.SetParameterFloat( propGain, newVolume )) {
                return false;
            }
            lastVolume = Math.Max( -60f, newVolume );
            lastVolumeAt = Now;
            return true;
        }
    }
}
