package org.rares.miner49er.issues.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.issues.model.IssuesDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesAdapter extends AbstractAdapter<IssuesViewHolder> {

    private static final String TAG = IssuesAdapter.class.getName();


    private List<IssueData> data = new ArrayList<>();

    public IssuesAdapter(final BaseInterfaces.ListItemClickListener listener) {
        clickListener = listener;
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_ISSUES);
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
        holder.bindData(data.get(position), getLastSelectedPosition() != -1);
//        Log.i(TAG, "onBindViewHolder: holder adapter position" + holder.getAdapterPosition());
    }

    @Override
    public String resolveData(int position) {
        String data = this.data.get(position).toString();
//        return data.get(position).toString();
        return getLastSelectedPosition() != -1 ? data.replace("Issue", "I") : data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void updateData(List<IssueData> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new IssuesDiff(data, newData));
        data = newData;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void accept(List list) throws Exception {
        Log.d(TAG, "accept() called with: list = [" + list + "]");
        updateData(list);
    }
}
