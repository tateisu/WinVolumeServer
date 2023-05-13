#nullable enable
#pragma warning disable IDE0079
#pragma warning disable IDE1006
#pragma warning disable IDE0060

using EmbedIO;
using EmbedIO.Routing;
using EmbedIO.WebApi;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Numerics;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WinVolumeServer {
    class ApiMedia : ApiBase {

        [StructLayout( LayoutKind.Sequential )]
        private struct POINT {
            public int x;
            public int y;
        }

        [StructLayout( LayoutKind.Sequential )]
        struct MouseInput {
            public int X;
            public int Y;
            public int Data;
            public int Flags;
            public int Time;
            public IntPtr ExtraInfo;
        }

        [StructLayout( LayoutKind.Sequential )]
        struct KeyboardInput {
            public short VirtualKey;
            public short ScanCode;
            public int Flags;
            public int Time;
            public IntPtr ExtraInfo;
        }

        [StructLayout( LayoutKind.Sequential )]
        struct HardwareInput {
            public int uMsg;
            public short wParamL;
            public short wParamH;
        }

        [StructLayout( LayoutKind.Sequential )]
        struct Input {
            public int Type;
            public InputUnion ui;
        }

        [StructLayout( LayoutKind.Explicit )]
        struct InputUnion {
            [FieldOffset( 0 )]
            public MouseInput Mouse;
            [FieldOffset( 0 )]
            public KeyboardInput Keyboard;
            [FieldOffset( 0 )]
            public HardwareInput Hardware;
        }

        //private const int KEYEVENTF_EXTENDEDKEY = 0x0001;
        private const int KEYEVENTF_KEYUP = 0x0002;
        //private const int KEYEVENTF_SCANCODE = 0x0008;
        //private const int KEYEVENTF_UNICODE = 0x0004;

        private const int MAPVK_VK_TO_VSC = 0;
        // private const int MAPVK_VSC_TO_VK = 1;

        private static class NativeMethods {
            [DllImport( "user32.dll", SetLastError = true )]
            public extern static void SendInput(int nInputs, Input[] pInputs, int cbsize);

            [DllImport( "user32.dll", EntryPoint = "MapVirtualKeyA" )]
            public extern static int MapVirtualKey(int wCode, int wMapType);

            //[DllImport("user32.dll", SetLastError = true)]
            //public extern static IntPtr GetMessageExtraInfo();
        }

        private static void SendInputKeyPressAndRelease(Keys key) {
            var inputs = new Input[ 2 ];

            var vsc = NativeMethods.MapVirtualKey( (int)key, MAPVK_VK_TO_VSC );

            inputs[ 0 ] = new Input {
                Type = 1 // KeyBoard = 1
            };
            inputs[ 0 ].ui.Keyboard.VirtualKey = (short)key;
            inputs[ 0 ].ui.Keyboard.ScanCode = (short)vsc;
            inputs[ 0 ].ui.Keyboard.Flags = 0;
            inputs[ 0 ].ui.Keyboard.Time = 0;
            inputs[ 0 ].ui.Keyboard.ExtraInfo = IntPtr.Zero;

            inputs[ 1 ] = new Input {
                Type = 1 // KeyBoard = 1
            };
            inputs[ 1 ].ui.Keyboard.VirtualKey = (short)key;
            inputs[ 1 ].ui.Keyboard.ScanCode = (short)vsc;
            inputs[ 1 ].ui.Keyboard.Flags = KEYEVENTF_KEYUP;
            inputs[ 1 ].ui.Keyboard.Time = 0;
            inputs[ 1 ].ui.Keyboard.ExtraInfo = IntPtr.Zero;

            NativeMethods.SendInput( inputs.Length, inputs, Marshal.SizeOf( inputs[ 0 ] ) );
        }

        [Route( HttpVerbs.Post, "/" )]
        public Task media([QueryField] string a, [FormData] NameValueCollection form) => checkPassword( () => {
            Console.WriteLine( $"a={a}" );
            if (a == "killAmazonMusic") {
                return killApp( new Regex( @"\AAmazon Music\.exe\s+(\d+)" ) );
            }

            var key = a switch {
                "playPause" => Keys.MediaPlayPause,
                "nextTrack" => Keys.MediaNextTrack,
                "previousTrack" => Keys.MediaPreviousTrack,
                "stop" => Keys.MediaStop,
                _ => Keys.None,
            };
            if (key == Keys.None) {
                return stringResponse( $"missing key mapping for action '{a}'", HttpStatusCode.BadRequest );
            }
            SendInputKeyPressAndRelease( key );
            return stringResponse( "ok.", HttpStatusCode.OK );
        } );

        private Task killApp(Regex reProcessId) {
            try {
                var winDir = Environment.GetEnvironmentVariable( "WINDIR" );
                if (!Directory.Exists( winDir )) {
                    return stringResponse( $"missing winDir {winDir}" );
                }

                var taskList = $"{winDir}\\system32\\tasklist.exe";
                if (!File.Exists( taskList )) {
                    return stringResponse( $"missing taskList {taskList}" );
                }
                var taskKill = $"{winDir}\\system32\\taskkill.exe";
                if (!File.Exists( taskKill )) {
                    return stringResponse( $"missing taskKill {taskKill}" );
                }

                for (var nTry = 1; nTry <= 10; ++nTry) {
                    var tasks = ApiBase.command( taskList );
                    if (tasks.Count == 0) {
                        throw new InvalidOperationException( "" );
                    }
                    var ids = new List<BigInteger>();
                    foreach (var line in tasks) {
                        try {
                            var m = reProcessId.Match( line );
                            if (!m.Success)
                                continue;
                            ids.Add( BigInteger.Parse( m.Groups[ 1 ].Value ) );
                        } catch (Exception ex) {
                            Console.WriteLine( ex );
                        }
                    }
                    if (ids.Count == 0) {
                        return stringResponse( "ok.", HttpStatusCode.OK );
                    }
                    ids.Sort();
                    var strIds = String.Join(
                        " ",
                        ids.ConvertAll( x => $"/PID {x}" )
                    );
                    ApiBase.command( taskKill, $"/f {strIds}" );
                    Thread.Sleep( 333 );
                }
                return stringResponse( "can't kill. retry exceeded." );
            } catch (Exception ex) {
                Debug.WriteLine( ex );
                return stringResponse( ex.ToString() );
            }
        }
    }
}
