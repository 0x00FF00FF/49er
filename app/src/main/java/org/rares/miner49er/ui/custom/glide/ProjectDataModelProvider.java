package org.rares.miner49er.ui.custom.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import lombok.Setter;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

import static org.rares.miner49er.BaseInterfaces.UTFEnc;

public class ProjectDataModelProvider implements ListPreloader.PreloadModelProvider<ProjectData> {

    private Context context;
    @Setter
    private List<ProjectData> projectDataList;

    public ProjectDataModelProvider(Context context, List<ProjectData> projectDataList) {
        this.context = context;
        this.projectDataList = projectDataList;
    }
public static final String TAG = ProjectDataModelProvider.class.getSimpleName();
    @NonNull
    @Override
    public List<ProjectData> getPreloadItems(int position) {
        if (position >= projectDataList.size()) {
//            Log.d(TAG, "getPreloadItems() called with: position = [" + position + "]  X_X");
            return Collections.emptyList();
        }
//        Log.d(TAG, "getPreloadItems() returned: " + projectDataList.get(position).getName());
        return Collections.singletonList(projectDataList.get(position));
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull ProjectData item) {
        GlideRequest<Drawable> glideRequest = null;
        String url = "";
        try {
            url = URLDecoder.decode(item.getPicture(), UTFEnc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getPreloadRequestBuilder: >>>> url: "  + url );
        glideRequest = GlideApp.with(context).load(url);

        return glideRequest;
    }
}