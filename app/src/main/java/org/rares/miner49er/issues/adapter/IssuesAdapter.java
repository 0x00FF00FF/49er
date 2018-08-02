package org.rares.miner49er.issues.adapter;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesAdapter extends AbstractAdapter<IssuesViewHolder> {

    private static final String TAG = IssuesAdapter.class.getName();

    private int issuesCount = 40;

    private List<IssueData> dataList = new ArrayList<>();

    public IssuesAdapter(final BaseInterfaces.ListItemClickListener listener, int tempIssuesNumber) {
        clickListener = listener;
        setIssuesCount(tempIssuesNumber);
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_ISSUES);
        initializeData();
//        clickListener.setViewHolderList(viewHolders);
//        setHasStableIds(true);
//      TODO: 8/1/18 turning stableIds on decreases performance??
//          and investigate possibilities
//          for viewHolders recycling or
//          clean removal + addition
    }

    @NonNull
    @Override
    public IssuesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View containerView =
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.resizeable_list_item, parent, false);

        final IssuesViewHolder ivh = new IssuesViewHolder(containerView);
        ivh.setItemClickListener(clickListener);

        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull IssuesViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bindData(dataList.get(position), getLastSelectedPosition() != -1);
//        Log.i(TAG, "onBindViewHolder: holder adapter position" + holder.getAdapterPosition());
    }

    @Override
    public String resolveData(int position) {
        String data = dataList.get(position).toString();
//        return dataList.get(position).toString();
        return getLastSelectedPosition() != -1 ? data.replace("Issue", "I") : data;
    }

    @Override
    public int getItemCount() {
        return issuesCount;
    }

    public void setIssuesCount(int count) {
        issuesCount = count;
    }

    private void initializeData() {
        for (int i = 0; i < getItemCount(); i++) {
            IssueData data = new IssueData();
            data.setId(NumberUtils.getNextProjectId());
            data.setName("Issue #" + i);
            dataList.add(data);
        }
    }

    @Override
    public void accept(List list) throws Exception {
        Log.d(TAG, "accept() called with: list = [" + list + "]");
    }
}
