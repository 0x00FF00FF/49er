package org.rares.miner49er.domain.projects.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.BaseInterfaces.ListItemEventListener;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.model.ProjectDiff;
import org.rares.miner49er.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author rares
 * @since v0.0.1, 22.09.2017.
 */

public class ProjectsAdapter
        extends AbstractAdapter<ProjectsViewHolder> {
//        extends RecyclerView.Adapter<ProjectsViewHolder> {

    private static final String TAG = ProjectsAdapter.class.getSimpleName();


    // It's way more effective to use DiffUtil.
    // By using DiffUtil, RV can properly recycle
    // items when swapping data. When using SortedList,
    // RV was always creating new ViewHolders. While using
    // SortedList and setting  hasStableIds(true) helps
    // in keeping memory usage low, but trades off CPU cycles.
    // Still (DiffUtil) vs (SortedList + hasStableIds(true)),
    // CPU usage is lower in favor of DiffUtil.
    // Lowest CPU usage is when you don't use any of them,
    // and just change the data and notify data set change,
    // but that is a very poor choice memory-wise.
    private List<ProjectData> data = new ArrayList<>();

    public ProjectsAdapter(final ListItemEventListener listener) {
        eventListener = listener;

        setMaxElevation(BaseInterfaces.MAX_ELEVATION_PROJECTS);

//        setHasStableIds(true);
//          if this is true,
//           the RV does a better job at reusing all available view holders
//          if this is false,
//           the RV uses a lot less CPU time when updating or creating
//           new views, but creates new view holders on each data set
//           change. rv also can't figure out where items are and can't
//           update them properly.
    }


    @NonNull
    @Override
    public ProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();

        View projectItemView =
                LayoutInflater.from(ctx)
                        .inflate(R.layout.resizeable_list_item, parent, false);

        final ProjectsViewHolder pvh = new ProjectsViewHolder(projectItemView);

        pvh.setItemClickListener(eventListener);

        if (unbinderHost != null) { // should be already set in the activity
            unbinderHost.registerUnbinder(pvh);
        } else {
            throw new IllegalStateException("Unbinder host is needed for memory management!");
        }

        Log.w(TAG, "onCreateViewHolder: " + pvh.hashCode());
        return pvh;
    }


    @Override
    public void onBindViewHolder(@NonNull ProjectsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.isToBeRebound()) {
//            holder.bindData(data.get(position), getLastSelectedPosition() != -1);
            holder.bindData(data.get(position), false);
        }
        Log.v(TAG, "onBindViewHolder() called with: " +
                "holder = [" + holder.hashCode() + "], " +
                "position = [" + position + "], " +
                "data = [" + data.get(position).getName() + "]");
    }

    @Override
    public void clearData() {
        updateList(Collections.emptyList());
    }

    @Override
    public String resolveData(int position) {
        ProjectData projectData = data.get(position);
        String name = projectData.getName();
        String minified = TextUtils.extractInitials(name);
//        return getLastSelectedPosition() != -1 ? minified : name;
        return name;
    }

//    @Override
//    public long getItemId(int position) {
////        Log.d(TAG, "getItemId: item at position: " + sortedData.get(position).getName()());
////        Log.i(TAG, "getItemId: position " + position);
//        int ret = -1;
//        if (customIds.size() > position) {
//            ret = customIds.get(position);
//        }
////        Log.d(TAG, "getItemId() returned: " + ret);
//        return ret;
//    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public int getItemViewType(int position) {
//        Log.i(TAG, "getItemViewType position: " + position);
        return R.layout.resizeable_list_item;
    }

    private void updateList(List<ProjectData> newData) {
//       new ProjectsSort().sort(newData, ProjectsInterfaces.SORT_TYPE_ALPHA_NUM);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProjectDiff(data, newData));

        data = newData;
        diffResult.dispatchUpdatesTo(this);

        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
//                Log.d(TAG, "onInserted() called with: position = [" + position + "], count = [" + count + "]");
            }

            @Override
            public void onRemoved(int position, int count) {
//                Log.d(TAG, "onRemoved() called with: position = [" + position + "], count = [" + count + "]");
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
//                Log.d(TAG, "onMoved() called with: fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "]");
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                int sp = getLastSelectedPosition();
                ItemViewProperties vp = new ProjectViewProperties();
                if (position == sp) {
                    if (payload instanceof Bundle) {
                        Bundle p = (Bundle) payload;
                        int id = p.getInt("id");
                        int color = p.getInt("color", 0);
                        if (color != 0) {
                            vp.setItemBgColor(color);
                        }
                        if (id != 0) {
                            vp.setId(id);
                        }
                        eventListener.onListItemChanged(vp);
                    }
                }
            }
        });
    }

    @Override
    public void accept(List list) throws Exception {
        updateList(list);
    }


}
