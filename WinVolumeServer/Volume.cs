#nullable enable
#pragma warning disable IDE1006

using CSCore.CoreAudioAPI;
using System;
using System.Diagnostics;

namespace WinVolumeServer {
    static class Volume {
        private static MMDevice? getCurrentDevice() {
            using var enumerator = new MMDeviceEnumerator();
            if (enumerator == null) return null;
            return enumerator.GetDefaultAudioEndpoint(DataFlow.Render, Role.Multimedia);
        }

        public static float? getVolume() {
            using var device = getCurrentDevice();
            if (device == null) return null;
            using var endpointVolume = AudioEndpointVolume.FromDevice(device);
            if (endpointVolume == null) return null;
            return endpointVolume.GetMasterVolumeLevel();
            //     Volume level in decibels. To get the range of volume levels obtained from this
            //     method, call the CSCore.CoreAudioAPI.AudioEndpointVolume.GetVolumeRange(System.Single@,System.Single@,System.Single@)
            //     method.
        }
        public static void setVolume(float newVolume) {
            using var device = getCurrentDevice();
            if (device == null) throw new InvalidOperationException("null GetDefaultAudioEndpoint");
            using var endpointVolume = AudioEndpointVolume.FromDevice(device);
            if (endpointVolume == null) throw new InvalidOperationException("null AudioEndpointVolume.FromDevice");
            try {
                endpointVolume.GetVolumeRange(out float volumeMinDB, out float volumeMaxDB, out float volumeIncrementDB);
                newVolume = newVolume.clip(volumeMinDB, volumeMaxDB);
                endpointVolume.SetMasterVolumeLevel(newVolume, Guid.Empty);
            }catch(Exception ex) {
                Debug.WriteLine($"{ex.GetType().Name} {ex.Message} {device.FriendlyName}");
            }
        }

        private static AudioSessionManager2? GetDefaultAudioSessionManager2(DataFlow dataFlow) {
            using var enumerator = new MMDeviceEnumerator();
            using var device = enumerator.GetDefaultAudioEndpoint(dataFlow, Role.Multimedia);
            Debug.WriteLine($"device.FriendlyName={device.FriendlyName}");
            var sessionManager = AudioSessionManager2.FromMMDevice(device);
            return sessionManager;
        }

        internal static string getDeviceName() {
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
