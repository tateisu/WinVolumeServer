#nullable enable
#pragma warning disable IDE1006

using EmbedIO;
using EmbedIO.Actions;
using EmbedIO.WebApi;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net;
using System.Linq;

namespace WinVolumeServer {
    class MyHttpServer {
        WebServer? httpServer;

        public void stop() {
            try {
                httpServer?.Dispose();
            } catch (Exception ex) {
                Debug.WriteLine(ex);
            }
            httpServer = null;
        }

        public void start(string port) {
            try {
                stop();
                var url = $"http://+:{port}/";
                var httpServer = new WebServer(
                        o => o.WithUrlPrefix(url).WithMode(HttpListenerMode.EmbedIO)
                    )
                    .WithWebApi("/volume", m => m.WithController<ApiVolume>())
                    .WithWebApi( "/media", m => m.WithController<ApiMedia>() )
                    .WithModule(new ActionModule("/", HttpVerbs.Any, ctx => {
                        ctx.Response.StatusCode = (int) HttpStatusCode.NotFound;
                        return ctx.SendStringAsync("not found", "text/plain", System.Text.Encoding.UTF8);
                    }));

                // .WithModule(new WebSocketChatModule("/chat"))
                // .WithModule(new WebSocketTerminalModule("/terminal"))
                // Add static files after other modules to avoid conflicts
                // .WithStaticFolder("/", HtmlRootPath, true, m => m.WithContentCaching(UseFileCache)) 
                // .WithAction("/test", HttpVerbs.Get, ctx => ctx.SendStringAsync("Hello World", "text/plain", System.Text.Encoding.UTF8))
                //.WithModule(new ActionModule("/", HttpVerbs.Any, ctx => {
                //ctx.SendStringAsync();
                //    ctx.SendDataAsync(new { Message = "Error" });

                // Listen for state changes.
                httpServer.StateChanged += (s, e) => Debug.WriteLine($"WebServer New State - {e.NewState}");


                this.httpServer = httpServer;

                // Once we've registered our modules and configured them, we call the RunAsync() method.
                httpServer.RunAsync();

                //httpServer.Use(new TcpListenerAdapter(new TcpListener(IPAddress.Any, int.Parse(port))));
                //httpServer.Use((context, next) => {
                //    try {
                //        handler.Invoke(context);
                //    } catch (Exception ex) {
                //        context.setStringResponse(HttpResponseCode.InternalServerError, ex.ToString());
                //    }
                //    return next();
                //});
                //httpServer.Start();

                var addressList = getLocalAddresses();
                addressList.Sort();
                var addressListText = string.Join( 
                    "", 
                    addressList.Select( x => $"\r\n{x}" ).ToArray()
                );
                Hub.pref.serverError = $"{Resource1.listeningOn}{addressListText}";
            } catch (Exception ex) {
                Debug.WriteLine(ex);
                Hub.pref.serverError = $"{ex.GetType().Name} {ex.Message}";
            }
        }

        static List<String> getLocalAddresses() {
            var dstList = new List<String>();
            if (System.Net.NetworkInformation.NetworkInterface.GetIsNetworkAvailable()) {
                var entry = Dns.GetHostEntry(Dns.GetHostName());
                foreach (var ip in entry.AddressList) {
                    dstList.Add(ip.ToString());
                }
            }
            return dstList;
        }
    }
}
