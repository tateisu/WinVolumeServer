#nullable enable
#pragma warning disable IDE1006

using CSCore.CoreAudioAPI;
using System;
using System.Diagnostics;
using System.Text.RegularExpressions;

namespace WinVolumeServer {
    static class Volume {
        private static MMDevice? getCurrentDevice() {
            using var enumerator = new MMDeviceEnumerator();
            if (enumerator == null) return null;
            return enumerator.GetDefaultAudioEndpoint(DataFlow.Render, Role.Multimedia);
        }

        private static readonly Regex reVoiceMeeter =
            new Regex( @"\bvoicemeeter\b", RegexOptions.IgnoreCase );

        public static float? getVolume() {
            using var device = getCurrentDevice();
            if (device == null) {
                return null;
            }
            if (reVoiceMeeter.IsMatch( device.FriendlyName )) {
                var v = Hub.voiceMeeter.getVolume();
                if (v != null) {
                    return v;
                }
            }
            using var endpointVolume = AudioEndpointVolume.FromDevice( device );
            if (endpointVolume == null)
                return null;
            return endpointVolume.GetMasterVolumeLevel();
            //     Volume level in decibels. To get the range of volume levels obtained from this
            //     method, call the CSCore.CoreAudioAPI.AudioEndpointVolume.GetVolumeRange(System.Single@,System.Single@,System.Single@)
            //     method.
        }
        public static void setVolume(float newVolume) {
            using var device = getCurrentDevice();
            if (device == null) throw new InvalidOperationException("null GetDefaultAudioEndpoint");

            if (reVoiceMeeter.IsMatch( device.FriendlyName )) {
                if (Hub.voiceMeeter.setVolume( newVolume ))
                    return;
            }

            using var endpointVolume = AudioEndpointVolume.FromDevice(device);
            if (endpointVolume == null) throw new InvalidOperationException("null AudioEndpointVolume.FromDevice");
            try {
                endpointVolume.GetVolumeRange(out var volumeMinDb, out var volumeMaxDb, out var volumeIncrementDB);
                Debug.WriteLine( $"newVolume={newVolume}, volumeRange={volumeMinDb}…{volumeMaxDb}" );
                newVolume = newVolume.clip(volumeMinDb, volumeMaxDb);
                endpointVolume.SetMasterVolumeLevel(newVolume, Guid.Empty);
            }catch(Exception ex) {
                Debug.WriteLine($"{ex.GetType().Name} {ex.Message} {device.FriendlyName}");
            }
        }

        internal static String getDeviceName() {
            try {
                using var device = getCurrentDevice();
                if (device == null) throw new InvalidOperationException("null GetDefaultAudioEndpoint");
                return device.FriendlyName;
            }catch(Exception ex) {
                Debug.WriteLine(ex);
                return "";
            }
        }
    }
}
