#nullable enable
#pragma warning disable IDE1006

using EmbedIO;
using EmbedIO.Routing;
using EmbedIO.WebApi;
using System;
using System.Collections.Specialized;

namespace WinVolumeServer {
    class VolumeApi : WebApiController {

        private void checkPassword() {
            var password = Hub.pref.password.Trim();
            if (password.Length == 0) return;

            var headers = HttpContext.Request.Headers;

            // リクエスト時刻をUnix time (ミリ秒単位)で
            var timeString = headers["X-Password-Time"];
            if (timeString == null || !long.TryParse(timeString, out long timeLong)) {
                throw new InvalidOperationException("invalid X-Password-Time header value");
            }
            // 現在時刻をUnix time (ミリ秒単位)で
            var now = DateTimeOffset.Now.ToUnixTimeMilliseconds();
            // 現在時刻とリクエスト時刻が30秒以上ズレたらエラー扱いにする
            if (Math.Abs(timeLong - now) >= 30L) {
                throw new InvalidOperationException("X-Password-Time header value is expired.");
            }
            var expectedDigest = $"{timeLong}:{password}".digestSha256().encodeBase64Url();
            var actualDigest = headers["X-Password-Digest"];
            if (expectedDigest != actualDigest) {
                throw new InvalidOperationException("X-Password-Digest header value not match.");
            }
        }


        private VolumeEntity createVolumeEntity() {
            return new VolumeEntity() {
                device = Volume.getDeviceName(),
                volume = Volume.getVolume(),
            };
        }

        [Route(HttpVerbs.Get, "/")]
        public VolumeEntity volumeGet() {
            checkPassword();
            return createVolumeEntity();
        }

        [Route(HttpVerbs.Post, "/")]
        public VolumeEntity volumePost([QueryField] string v, [FormData] NameValueCollection form) {
            checkPassword();
            if (!float.TryParse(v, out float volumeFloat)) {
                throw new InvalidOperationException("invalid parameter 'v'");
            }
            Volume.setVolume(volumeFloat);
            var entity = createVolumeEntity();
            return entity;
        }
    }
}
