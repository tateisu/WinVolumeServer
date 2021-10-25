#nullable enable
#pragma warning disable IDE1006


using Swan.Formatters;

namespace WinVolumeServer {
    class VolumeEntity {
        [JsonProperty("device")]
        public string device { get; set; } = "";

        [JsonProperty("volume")]
        public float? volume { get; set; } = null;
    }
}
