#nullable enable
#pragma warning disable IDE1006

using EmbedIO;
using EmbedIO.Routing;
using EmbedIO.WebApi;
using System;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Net;
using System.Threading.Tasks;

namespace WinVolumeServer {
    class VolumeApi : WebApiController {

        // return false if error was set
        private Task? checkPassword() {
            var password = Hub.pref.password.Trim();
            if (password.Length > 0) {
                var headers = HttpContext.Request.Headers;

                // リクエスト時刻をUnix time (ミリ秒単位)で
                var timeString = headers["X-Password-Time"];
                var actualDigest = headers["X-Password-Digest"];
                if (actualDigest == null || timeString == null || !long.TryParse(timeString, out long timeLong)) {
                    return HttpContext.setStringResponse(HttpStatusCode.BadRequest, "missing password headers.");
                }
                // 現在時刻をUnix time (ミリ秒単位)で
                var now = DateTimeOffset.Now.ToUnixTimeMilliseconds();
                // 現在時刻とリクエスト時刻が30秒以上ズレたらエラー扱いにする
                if (Math.Abs(timeLong - now) >= 30000L) {
                    return HttpContext.setStringResponse(HttpStatusCode.BadRequest, "please set watch-clock correctly.");
                }
                var expectedDigest = $"{timeLong}:{password}".digestSha256().encodeBase64Url();
                if (expectedDigest != actualDigest) {
                    return HttpContext.setStringResponse(HttpStatusCode.BadRequest, "password digest not match.");
                }
            }
            return null;
        }


        private Task createVolumeEntity() {
            try {
                return HttpContext.setJsonResponse(
                    HttpStatusCode.OK,
                    new VolumeEntity() {
                        device = Volume.getDeviceName(),
                        volume = Volume.getVolume(),
                    }
                );
            } catch (Exception ex) {
                Debug.WriteLine(ex);
                return HttpContext.setStringResponse(HttpStatusCode.InternalServerError, ex.ToString());
            }
        }

        [Route(HttpVerbs.Get, "/")]
        public Task volumeGet() {
            var errorTask = checkPassword();
            if (errorTask != null) return errorTask;
            return createVolumeEntity();
        }

        [Route(HttpVerbs.Post, "/")]
        public Task volumePost([QueryField] string v, [FormData] NameValueCollection form) {
            var errorTask = checkPassword();
            if (errorTask != null) return errorTask;

            if (!float.TryParse(v, out float volumeFloat)) {
                return HttpContext.setStringResponse(HttpStatusCode.BadRequest, "invalid parameter 'v'");
            }
            Volume.setVolume(volumeFloat);
            return createVolumeEntity();
        }
    }
}
