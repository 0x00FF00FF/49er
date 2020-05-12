package org.rares.miner49er.domain.projects.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import lombok.Getter;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.BaseInterfaces.ListItemEventListener;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.model.ProjectDiff;
import org.rares.miner49er.domain.projects.ui.viewholder.ProjectsViewHolder;
import org.rares.miner49er.util.PermissionsUtil;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author rares
 * @since v0.0.1, 22.09.2017.
 */

public class ProjectsAdapter
        extends AbstractAdapter<ProjectsViewHolder, ProjectData> {

    private static final String TAG = ProjectsAdapter.class.getSimpleName();


    // It's way more effective to use DiffUtil.
    // By using DiffUtil, RV can properly recycle
    // items when swapping data. When using SortedList,
    // RV was always creating new ViewHolders. Using
    // SortedList and setting  hasStableIds(true) helps
    // in keeping memory usage low, but trades off CPU cycles.
    // Still (DiffUtil) vs (SortedList + hasStableIds(true)),
    // CPU usage is lower in favor of DiffUtil.
    // Lowest CPU usage is when you don't use any of them,
    // and just change the data and notify data set change,
    // but that is a very poor choice memory-wise.
    @Getter
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
                        .inflate(R.layout.list_item_project, parent, false);

        final ProjectsViewHolder pvh = new ProjectsViewHolder(projectItemView);

        pvh.setItemClickListener(eventListener);

        if (unbinderHost != null) { // should be already set in the activity
            unbinderHost.registerUnbinder(pvh);
        } else {
            throw new IllegalStateException("Unbinder host is needed for memory management!");
        }

//        Log.w(TAG, "onCreateViewHolder: " + pvh.hashCode());
        return pvh;
    }


    @Override
    public void onBindViewHolder(@NonNull ProjectsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        // newHolder variable to be used for checking if the event
        // listener should be made aware of the changes that happen
        // in the view holder. the issue behind this is not to change
        // the toolbar/action bar title/subtitle when not needed.
        // this may be happening when the sticky layout manager needs
        // information about the selected view: rv needs to create a
        // holder for the selected position because the selected position
        // holder is locked by the StickyLinearLayoutManager and at
        // times it needs to be refreshed
//        final boolean newHolder = holder.getItemData() == null;

        holder.bindData(
                data.get(position),
                getLastSelectedPosition() != -1,
                position == getLastSelectedPosition());

        if (position == getLastSelectedPosition()) {
            eventListener.onListItemChanged(holder.getItemProperties());
        }

//        Log.v(TAG, "onBindViewHolder() called with: " +
//                "holder = [" + holder.hashCode() + "], " +
//                "position = [" + position + "], " +
//                "data = [" + data.get(position).getName() + "]");
    }

    @Override
    public void clearData() {
        updateList(Collections.emptyList());
    }

    @Override
    public String resolveData(int position, boolean forceFullData) {
        ProjectData projectData = data.get(position);
        String name = projectData.getName();
        String minified = TextUtils.extractInitials(name);
        return getLastSelectedPosition() != -1 ? forceFullData ? name : minified : name;
    }

    @Override
    public ProjectData getDisplayData(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= data.size()) {
            return null;
        }
        return data.get(adapterPosition);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


//    @Override
//    public int getItemViewType(int position) {
//        return R.layout.resizeable_list_item;
//    }

    private void updateList(List<ProjectData> newData) {
//        Log.d(TAG, "updateList() called with: " +
//            "newData = [" + (newData == null ? "null" : (newData.size() + " items")) + "], " +
//            "oldData = [" + (data == null ? "null" : (data.size() + " items")) + "]");
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProjectDiff(data, newData));
        data = newData;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void accept(List list) {
        updateList(list);
    }

    @Override
    public String getToolbarData(Context context, int position) {
        return UiUtil.populateInfoString(context.getResources().getString(R.string._projects_info_template), data.get(position));
    }

    @Override
    public boolean canRemoveItem(int position) {
        if (data != null && position > -1 && position < data.size()) {
            return PermissionsUtil.canRemoveProject(data.get(position));
        }
        return false;
    }
}
