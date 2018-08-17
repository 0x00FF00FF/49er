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
import org.rares.miner49er.util.TextUtils;

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
        Log.d(TAG, "onCreateViewHolder() called with: parent = [" + parent.hashCode() + "], viewType = [" + viewType + "]");
        View containerView =
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.resizeable_list_item, parent, false);

        final IssuesViewHolder ivh = new IssuesViewHolder(containerView);
        ivh.setItemClickListener(clickListener);

        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull IssuesViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder.hashCode() + "], position = [" + position + "]");
        super.onBindViewHolder(holder, position);
        holder.bindData(data.get(position), getLastSelectedPosition() != -1);
//        Log.i(TAG, "onBindViewHolder: holder adapter position" + holder.getAdapterPosition());
    }

    @Override
    public String resolveData(int position) {
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
//        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
//            @Override
//            public void onInserted(int position, int count) {
//                Log.d(TAG, "onInserted() called with: position = [" + position + "], count = [" + count + "]");
//            }
//
//            @Override
//            public void onRemoved(int position, int count) {
//                Log.d(TAG, "onRemoved() called with: position = [" + position + "], count = [" + count + "]");
//            }
//
//            @Override
//            public void onMoved(int fromPosition, int toPosition) {
//                Log.d(TAG, "onMoved() called with: fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "]");
//            }
//
//            @Override
//            public void onChanged(int position, int count, Object payload) {
//                Log.d(TAG, "onChanged() called with: position = [" + position + "], count = [" + count + "], payload = [" + payload + "]");
//            }
//        });
    }

    @Override
    public void accept(List list) throws Exception {
//        Log.d(TAG, "accept() called with: list = [" + list + "]");
        updateData(list);
    }
}
