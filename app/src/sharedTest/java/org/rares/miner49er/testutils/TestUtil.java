package org.rares.miner49er.testutils;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestUtil {

    public static void copyAssets() {
        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files != null) {
            for (String filename : files) {
                copyAsset(assetManager, filename);
            }
        }
    }

    public static File copyAsset(AssetManager assetManager, String filename) {
        File outFile = new File(InstrumentationRegistry.getInstrumentation().getContext().getExternalFilesDir(null), filename);
        try (InputStream in = assetManager.open(filename);
             OutputStream out = new FileOutputStream(outFile);
        ) {
            copyFile(in, out);
        } catch (IOException e) {
            System.err.println("Failed to copy asset file: " + filename);
        }
        System.out.println(">>> " + outFile.getAbsolutePath());
        return outFile;
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return targetContext.getResources().getString(id);
    }

    public static int getResourceInt(int id) {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return targetContext.getResources().getInteger(id);
    }
}
