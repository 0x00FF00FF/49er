package org.rares.miner49er.issues.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.issues.model.IssuesDiff;
import org.rares.miner49er.projects.adapter.ProjectViewProperties;
import org.rares.miner49er.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesAdapter extends AbstractAdapter<IssuesViewHolder> {

    private static final String TAG = IssuesAdapter.class.getSimpleName();

    private List<IssueData> data = new ArrayList<>();

    public IssuesAdapter(final BaseInterfaces.ListItemEventListener listener) {
        eventListener = listener;
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_ISSUES);
    }

    @NonNull
    @Override
    public IssuesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View containerView =
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.resizeable_list_item, parent, false);

        final IssuesViewHolder ivh = new IssuesViewHolder(containerView);
        ivh.setItemClickListener(eventListener);

        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull IssuesViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
        super.onBindViewHolder(holder, position);
        holder.bindData(data.get(position), getLastSelectedPosition() != -1);
    }

    @Override
    public void clearData() {
        updateData(Collections.emptyList());
    }

    @Override
    public String resolveData(int position) {
        if (position < 0 || position >= data.size()) {
            return null;
        }
        String issueName = this.data.get(position).toString();
        return getLastSelectedPosition() != -1 ? TextUtils.extractInitials(issueName) : issueName;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void updateData(List<IssueData> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new IssuesDiff(data, newData));
        data = newData;
        diffResult.dispatchUpdatesTo(this);
        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d(TAG, "onInserted() called with: position = [" + position + "], count = [" + count + "]");
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved() called with: position = [" + position + "], count = [" + count + "]");
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d(TAG, "onMoved() called with: fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "]");
                // dispatch towards layout manager?
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                int sp = getLastSelectedPosition();
                ItemViewProperties vp = new ProjectViewProperties();
                Log.v(TAG, "onChanged() called with: " +
                        "selectedPos = [" + sp + "], " +
                        "position = [" + position + "], " +
                        "count = [" + count + "], " +
                        "payload = [" + payload + "]");
                if (position == sp) {
                    if (payload instanceof Bundle) {
                        Bundle p = (Bundle) payload;
                        int id = p.getInt("id");
                        int color = p.getInt("Color", 0);
                        Log.d(TAG, "onChanged: >>>>> TRIGGERED >> new color >> " + color);
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
//        Log.d(TAG, "accept() called with: list = [" + list + "]");
        updateData(list);
    }
}
