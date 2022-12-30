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
    class ApiVolume : ApiBase {

        [Route( HttpVerbs.Get, "/" )]
        public Task volumeGet() =>
            checkPassword( () => createVolumeEntity() );

        [Route( HttpVerbs.Post, "/" )]
        public Task volumePost([QueryField] string v, [FormData] NameValueCollection form) =>
            checkPassword( () => {
                if (!float.TryParse( v, out float volumeFloat )) {
                    return stringResponse( "invalid parameter 'v'", HttpStatusCode.BadRequest );
                }
                Volume.setVolume( volumeFloat );
                return createVolumeEntity();
            } );

        // GETでもPOSTでも、応答は現在のボリューム値
        private Task createVolumeEntity() =>
            HttpContext.setJsonResponse(
                    HttpStatusCode.OK,
                    new VolumeEntity() {
                        device = Volume.getDeviceName(),
                        volume = Volume.getVolume(),
                    }
                );
    }
}
