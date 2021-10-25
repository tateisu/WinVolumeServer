#nullable enable
#pragma warning disable IDE1006


using EmbedIO;
using NeoSmart.Utils;
using System;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace WinVolumeServer {
    public static class Ext {

        public static byte[] digestSha256(this string src) {
            var input = Encoding.UTF8.GetBytes(src);
            var provider = new SHA256CryptoServiceProvider();
            return provider.ComputeHash(input);
        }

        public static String encodeBase64Url(this byte[] src) {
            return UrlBase64.Encode(src);
        }

        public static Task setStringResponse(this IHttpContext context, HttpStatusCode code, String message) {
            context.Response.StatusCode = (int)code;
            return context.SendStringAsync(message, "text/plain", Encoding.UTF8);
        }

        public static Task setJsonResponse(this IHttpContext context, HttpStatusCode code, object data) {
            context.Response.StatusCode = (int)code;
            return context.SendDataAsync(data);
        }

        public static float clip(this float value,float min,float max) {
            return Math.Max(min, Math.Min(max, value));
        }
    }
}
