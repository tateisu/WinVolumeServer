#nullable enable
#pragma warning disable IDE1006

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net;
using System.Threading.Tasks;
using EmbedIO;
using EmbedIO.Routing;
using EmbedIO.WebApi;

namespace WinVolumeServer {
    public class ApiBase : WebApiController {

        public Task stringResponse(
            string message,
            HttpStatusCode statusCode = HttpStatusCode.InternalServerError
        ) => HttpContext.setStringResponse(
                statusCode,
                message
            );

        public Task checkPassword(Func<Task> block) {
            try {
                var password = Hub.pref.password.Trim();
                if (password.Length > 0) {
                    var headers = HttpContext.Request.Headers;

                    // リクエスト時刻をUnix time (ミリ秒単位)で
                    var timeString = headers[ "X-Password-Time" ];
                    var actualDigest = headers[ "X-Password-Digest" ];
                    if (actualDigest == null || timeString == null || !long.TryParse( timeString, out var timeLong )) {
                        return stringResponse( "missing password headers.", HttpStatusCode.BadRequest );
                    }
                    // 現在時刻をUnix time (ミリ秒単位)で
                    var now = DateTimeOffset.Now.ToUnixTimeMilliseconds();
                    // 現在時刻とリクエスト時刻が30秒以上ズレたらエラー扱いにする
                    if (Math.Abs( timeLong - now ) >= 30000L) {
                        return stringResponse( "please set watch-clock correctly.", HttpStatusCode.BadRequest );
                    }
                    var expectedDigest = $"{timeLong}:{password}".digestSha256().encodeBase64Url();
                    if (expectedDigest != actualDigest) {
                        return stringResponse( "password digest not match.", HttpStatusCode.BadRequest );
                    }
                }
                return block();
            } catch (Exception ex) {
                Debug.WriteLine( ex );
                return stringResponse( ex.ToString() );
            }
        }

        public static List<string> command(
            string fileName,
            string arguments = ""
        ) {
            var lines = new List<string>();
            var psInfo = new ProcessStartInfo {
                FileName = fileName,
                Arguments = arguments,
                CreateNoWindow = true,
                UseShellExecute = false,
                RedirectStandardOutput = true
            };
            var p = Process.Start( psInfo );
            while (true) {
                var line = p.StandardOutput.ReadLine();
                if (line == null)
                    break;
                lines.Add( line );
            }
            p.WaitForExit();
            return lines;
        }
    }
}
