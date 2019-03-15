package org.rares.miner49er.ui.custom.glide.preload;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.RequestBuilder;
import com.pushtorefresh.storio3.Optional;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.ui.custom.glide.GlideApp;
import org.rares.miner49er.ui.custom.glide.GlideRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.rares.miner49er.BaseInterfaces.UTFEnc;

public class ProjectDataModelProvider implements MultipleListPreloader.PreloadModelProvider<String> {

    private Context context;


    public ProjectDataModelProvider(Context context) {
        this.context = context;
    }

//    public static final String TAG = ProjectDataModelProvider.class.getSimpleName();

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        List<ProjectData> projectDataList = ViewModelCache.getInstance().getCache(ProjectData.class).getData(Optional.of(null));
        // todo: if mismatches appear maybe switch to adapter data.
        if (position >= projectDataList.size()) {
//            Log.d(TAG, "getPreloadItems() called with: position = [" + position + "/" + projectDataList.size() + "]  X_X");
            return Collections.emptyList();
        }
        String url = projectDataList.get(position).getPicture();
//        Log.d(TAG, "getPreloadItems() returned: " + projectDataList.get(position).getName());
        List<String> list = new ArrayList<>();
        list.add(url);
        return list;
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull String item) {
        GlideRequest<Drawable> glideRequest = null;
        String url = "";
        try {
            url = URLDecoder.decode(item, UTFEnc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getPreloadRequestBuilder: >>>> url: " + url);
        glideRequest = GlideApp.with(context).load(url);

        return glideRequest;
    }
}