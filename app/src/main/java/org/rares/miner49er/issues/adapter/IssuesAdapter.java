package org.rares.miner49er.issues.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.issues.IssuesUiOps;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesAdapter extends AbstractAdapter<IssuesViewHolder> {

    private static final String TAG = IssuesAdapter.class.getName();

    private int issuesCount = 40;

    private List<IssueData> dataList = new ArrayList<>();

    public IssuesAdapter(final IssuesUiOps uiOps, int tempIssuesNumber) {
        ops = uiOps;
        setIssuesCount(tempIssuesNumber);
        initializeData();
        uiOps.setViewHolderList(viewHolders);
//        setHasStableIds(true);
    }

    @Override
    public IssuesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View containerView =
                LayoutInflater.from(parent.getContext()).
                inflate(R.layout.resizeable_list_item, parent, false);

        final IssuesViewHolder ivh = new IssuesViewHolder(containerView);
        ivh.setItemClickListener(ops);
        ivh.setMaxItemElevation(ops.getMaxElevation() + 2);
        // ^ careful with this if/when using shared rvp
        return ivh;
    }

    @Override
    public void onBindViewHolder(IssuesViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bindData(dataList.get(position));
        Log.i(TAG, "onBindViewHolder: holder adapter position" + holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return issuesCount;
    }

    public void setIssuesCount(int count){
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
}
