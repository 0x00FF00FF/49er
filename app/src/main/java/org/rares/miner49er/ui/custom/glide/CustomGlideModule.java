package org.rares.miner49er.ui.custom.glide;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import org.rares.miner49er._abstract.NetworkingService;

import java.io.InputStream;

@GlideModule
public class CustomGlideModule extends AppGlideModule {
    public static final String TAG = CustomGlideModule.class.getSimpleName();

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkingService.RestServiceGenerator.INSTANCE.getHttpClient());
        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setLogLevel(Log.ERROR);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}