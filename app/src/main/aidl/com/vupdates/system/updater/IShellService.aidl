package com.vupdates.system.updater;

import android.os.ParcelFileDescriptor;
import com.vupdates.system.updater.ILogCallback;

interface IShellService {
    ParcelFileDescriptor startRecording(
        String audioSource,
        String audioCodec,
        int audioBitRate,
        String serverPath,
        boolean isDebuggingModeEnabled, // For debugging purposes, if true, the service will log additional information and change some logging behavior.
        ILogCallback appLoggerCallback
    ) = 1;

    void stopRecording() = 2;

    boolean isRecording() = 3;

    // The special Shizuku transaction code for "destroy" process
    void destroy() = 16777114;
}
