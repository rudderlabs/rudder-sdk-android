package com.rudderstack.android.sdk.core.util;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.ReportManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {
    public static @Nullable OutputStream getGzipOutputStream(OutputStream outputStream) {
        try {
            return new GZIPOutputStream(outputStream);
        } catch (IOException e) {
            ReportManager.reportError(e);
            e.printStackTrace();
        }
        return null;
    }
}
