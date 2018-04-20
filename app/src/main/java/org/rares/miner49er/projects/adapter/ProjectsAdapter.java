package org.rares.miner49er.projects.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.BaseInterfaces.ListItemClickListener;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.projects.model.ProjectData;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO: 11.10.2017 maybe create an abstract adapter?

/**
 * @author rares
 * @since v0.0.1, 22.09.2017.
 */

public class ProjectsAdapter
        extends AbstractAdapter<ProjectsViewHolder> {
//        extends RecyclerView.Adapter<ProjectsViewHolder> {

    private int currentSortType = 1;

    public static final int SORT_TYPE_SIMPLE = 0;
    public static final int SORT_TYPE_ALPHA_NUM = 1;
    public static final int SORT_TYPE_RECENT = 2;
    public static final int SORT_TYPE_FAVORITES = 3;

    private static final String TAG = ProjectsAdapter.class.getSimpleName();


    // is this even needed? ops should never become null...

    private final String[] dummyData = {
            "Project 1",
            "Project 2",
            "Project 3",
            "Project 4",
            "Project 5",
            "Project 6",
            "Project 9",
            "Project 7",
            "Project 8",
            "Project 10",
            "Project 14",
            "Project 11",
            "Project 12",
            "Project 13",
            "Project 15",
            "Project 16",
            "Project 17",
            "Project 18"
    };

    private final String[] projectsColors = {
            "#cbbeb5",
            "#e9aac8",
            "#c9aac8",
            "#a9aac8",
            "#96e7cf",
            "#96c7cf",
            "#96a7cf",
            "#baa0a7",
            "#bac0c7",
            "#bae0e7",
            "#b5a1d1",
            "#b5c1d1",
            "#b5e1d1",
            "#dfc6d0",
            "#ffe6d0",
            "#bfa6d0",
            "#ecbcd7",
            "#d8d3e4",
            "#232467",
            "#644783",
            "#0f54ad",
            "#000033",
            "#282531",
            "#383640",
            "#282236",
            "#2a233c",
            "#2e4f70",
            "#44344e",
            "#6c619e",
            "#7070ff"
    };

    private SortedList<ProjectData> sortedData = null; // TODO: 20.02.2018 see how to use diff util stuff.
    private SortedListAdapterCallback<ProjectData> slCallback = null;

    private List<Integer> customIds = new ArrayList<>();


    public ProjectsAdapter(final ListItemClickListener listener) {
        clickListener = listener;
        slCallback = getNewAlphaNumSort(this);
        sortedData = new SortedList<>(ProjectData.class, slCallback);
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_PROJECTS);
        initializeData();
//        setHasStableIds(true);
    }

    /**
     * - initializes the data. <br/>
     * - does not check if the sorted list is already empty.
     */
    private void initializeData() {
        sortedData.beginBatchedUpdates();
        for (int i = 0; i < dummyData.length; i++) {
            ProjectData projectData = new ProjectData();
            projectData.setProjectName(dummyData[i]);
            projectData.setColor(projectsColors[i]);
            sortedData.add(projectData);
            customIds.add(NumberUtils.getNextProjectId());
        }
        sortedData.endBatchedUpdates();

        setParentColor(Color.parseColor(projectsColors[0]));
    }


    @Override
    public ProjectsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() called with: parent = [" + parent + "], viewType = [" + viewType + "]");
        Context ctx = parent.getContext();

        View projectItemView =
                LayoutInflater.from(ctx)
                        .inflate(R.layout.resizeable_list_item, parent, false);

        final ProjectsViewHolder pvh = new ProjectsViewHolder(projectItemView);
        pvh.setItemClickListener(clickListener);
//        pvh.setMaxItemElevation(getMaxElevation() + 2);
        return pvh;
    }


    @Override
    public void onBindViewHolder(ProjectsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.isToBeRebound()) {
            holder.getItemProperties().setItemContainerCustomId(customIds.get(position));
            holder.bindData(sortedData.get(position));
        }
    }

    public void removeItem() {
        int removeAtPosition = 0;
        if (sortedData.size() == 0) {
            initializeData();
            return;
        }

        customIds.remove(removeAtPosition);
        sortedData.removeItemAt(removeAtPosition);
    }

    @Override
    public long getItemId(int position) {
//        Log.d(TAG, "getItemId: item at position: " + sortedData.get(position).getProjectName());
//        Log.i(TAG, "getItemId: position " + position);
        int ret = -1;
        if (customIds.size() > position) {
            ret = customIds.get(position);
        }
//        Log.d(TAG, "getItemId() returned: " + ret);
        return ret;
    }

    @Override
    public int getItemCount() {
        return sortedData.size();
    }


    @Override
    public int getItemViewType(int position) {
//        Log.i(TAG, "getItemViewType position: " + position);
        return R.layout.resizeable_list_item;
    }


    /**
     * Change the sorting type for the list of projects. <br/>
     * Should/Can only be used for local data.
     *
     * @param sortType - type of sorting.
     *                 can be any of:
     *                 <p>
     *                 SORT_TYPE_SIMPLE         <br/>
     *                 SORT_TYPE_ALPHA_NUM      <br/>
     *                 SORT_TYPE_RECENT         <br/>
     *                 SORT_TYPE_FAVORITES      <br/>
     *                 </p>
     */
    public void changeSortType(int sortType) {
        Log.d(TAG, "changeSortType: new " + sortType);
        Log.d(TAG, "changeSortType: current " + sortType);
        if (currentSortType == SORT_TYPE_SIMPLE) {
            slCallback = getNewAlphaNumSort(this);
            currentSortType = SORT_TYPE_ALPHA_NUM;
        } else {
            slCallback = getNewSimpleSort(this);
            currentSortType = SORT_TYPE_SIMPLE;
        }
//        if (SORT_TYPE_ALPHA_NUM == sortType) {
//            slCallback = getNewAlphaNumSort(this);
//        }
//        if (SORT_TYPE_SIMPLE == sortType) {
//            slCallback = getNewSimpleSort(this);
//        }

        sortedData = new SortedList<>(ProjectData.class, slCallback);
        initializeData();
        notifyDataSetChanged();
    }

    private SortedListAdapterCallback<ProjectData> getNewSimpleSort(ProjectsAdapter adapter) {
        return new SortedListAdapterCallback<ProjectData>(adapter) {

            @Override
            public int compare(ProjectData o1, ProjectData o2) {
                int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getProjectName(), o2.getProjectName());
                if (res == 0) {
                    res = o1.getProjectName().compareTo(o2.getProjectName());
                }
                return res;
            }

            @Override
            public boolean areContentsTheSame(ProjectData oldItem, ProjectData newItem) {
                return oldItem.areContentsTheSameWith(newItem);
            }

            @Override
            public boolean areItemsTheSame(ProjectData item1, ProjectData item2) {
                return item1.equals(item2);
            }
        };
    }

    private SortedListAdapterCallback<ProjectData> getNewAlphaNumSort(ProjectsAdapter adapter) {
        return new SortedListAdapterCallback<ProjectData>(adapter) {
            @Override
            public int compare(ProjectData o1, ProjectData o2) {
                return new InternalNumberComparator().compare(
                        o1.getProjectName(),
                        o2.getProjectName());
            }

            @Override
            public boolean areContentsTheSame(ProjectData oldItem, ProjectData newItem) {
                return oldItem.areContentsTheSameWith(newItem);
            }

            @Override
            public boolean areItemsTheSame(ProjectData item1, ProjectData item2) {
                return item1.equals(item2);
            }
        };
    }

    class InternalNumberComparator implements Comparator {
        private Pattern splitter = Pattern.compile("(\\d+|\\D+)");

        public int compare(Object o1, Object o2) {
            // I deliberately use the Java 1.4 syntax,
            // all this can be improved with 1.5's generics
            String s1 = (String) o1, s2 = (String) o2;
            // We split each string as runs of number/non-number strings
            ArrayList sa1 = split(s1);
            ArrayList sa2 = split(s2);
            // Nothing or different structure
            if (sa1.size() == 0 || sa1.size() != sa2.size()) {
                // Just compare the original strings
                return s1.compareTo(s2);
            }
            int i = 0;
            String si1 = "";
            String si2 = "";
            // Compare beginning of string
            for (; i < sa1.size(); i++) {
                si1 = (String) sa1.get(i);
                si2 = (String) sa2.get(i);
                if (!si1.equals(si2))
                    break;  // Until we find a difference
            }
            // No difference found?
            if (i == sa1.size())
                return 0; // Same strings!

            // Try to convert the different run of characters to number
            int val1, val2;
            try {
                val1 = Integer.parseInt(si1);
                val2 = Integer.parseInt(si2);
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);  // Strings differ on a non-number
            }

            // Compare remainder of string
            for (i++; i < sa1.size(); i++) {
                si1 = (String) sa1.get(i);
                si2 = (String) sa2.get(i);
                if (!si1.equals(si2)) {
                    return s1.compareTo(s2);  // Strings differ
                }
            }

            // Here, the strings differ only on a number
            return val1 < val2 ? -1 : 1;
        }

        ArrayList<String> split(String s) {
            ArrayList<String> r = new ArrayList<>();
            Matcher matcher = splitter.matcher(s);
            while (matcher.find()) {
                String m = matcher.group(1);
                r.add(m);
            }
            return r;
        }

    }

}
