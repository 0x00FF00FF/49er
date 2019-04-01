package org.rares.miner49er.domain.issues.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.model.IssuesDiff;
import org.rares.miner49er.domain.issues.ui.viewholder.IssuesViewHolder;
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
                        inflate(R.layout.list_item_issue, parent, false);

        final IssuesViewHolder ivh = new IssuesViewHolder(containerView);
        ivh.setItemClickListener(eventListener);

        if (unbinderHost != null) {
            unbinderHost.registerUnbinder(ivh);
        } else {
            throw new IllegalStateException("Unbinder host is needed for memory management!");
        }

        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull IssuesViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
        super.onBindViewHolder(holder, position);
        IssueData issueData = data.get(position);
//        List<TimeEntryData> tedata = issueData.getTimeEntries();
//        int hourCount = 0;
//        for (TimeEntryData ted : tedata) {
//            if (ted != null) {
//                hourCount += ted.getHours();
//            }
//        }
        holder.bindData(
                issueData,
                getLastSelectedPosition() != -1,
                position == getLastSelectedPosition());
//        Log.i(TAG, "onBindViewHolder: position: ["
//                + position + "] data: ["
//                + issueData.getName() + "]["
//                + tedata.size() + "]" + " hours: "
//                + hourCount
//        );
        if (position == getLastSelectedPosition()) {
            eventListener.onListItemChanged(holder.getItemProperties());
        }
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
        String issueName = this.data.get(position).getName();
        return getLastSelectedPosition() != -1 ? TextUtils.extractInitials(issueName) : issueName;
    }

    @Override
    public Object getDisplayData(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= data.size()) {
            return null;
        }
        return data.get(adapterPosition);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void updateData(List<IssueData> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new IssuesDiff(data, newData));
        data = newData;
        diffResult.dispatchUpdatesTo(this);
//        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {});
    }


    @Override
    public void accept(List list) throws Exception {
//        Log.d(TAG, "accept() called with: list = [" + list + "]");
//        for (int i = 0; i < list.size(); i++) {
//            IssueData issueData = (IssueData) list.get(i);
//            Log.d(TAG, "accept: " + issueData.getName());
//        }
        updateData(list);
    }
}
