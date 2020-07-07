package org.rares.miner49er.domain.issues.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import lombok.Getter;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.model.IssuesDiff;
import org.rares.miner49er.domain.issues.ui.viewholder.IssuesViewHolder;
import org.rares.miner49er.util.PermissionsUtil;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesAdapter extends AbstractAdapter<IssuesViewHolder, IssueData> {

    private static final String TAG = IssuesAdapter.class.getSimpleName();

    @Getter
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

//        if (unbinderHost != null) {
//            unbinderHost.registerUnbinder(ivh);
//        } else {
//            throw new IllegalStateException("Unbinder host is needed for memory management!");
//        }

        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull IssuesViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
//        Log.i(TAG, "onBindViewHolder: old data: " + holder.getItemText());
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
    public String resolveData(int position, boolean forceFullData) {
        if (position < 0 || position >= data.size()) {
            return null;
        }
        String issueName = this.data.get(position).getName();
        return getLastSelectedPosition() != -1 ? forceFullData ? issueName : TextUtils.extractInitials(issueName) : issueName;
    }

    @Override
    public IssueData getDisplayData(int adapterPosition) {
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
//        String name = "";
//        if (getLastSelectedPosition() > -1) {
//          name = getSelected().getName();
//        }
//        Log.i(TAG, "updateData: >s " + name);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new IssuesDiff(data, newData));
        diffResult.dispatchUpdatesTo(this);
/*        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d(TAG, "onInserted() called with: position = [" +
                    (position == getLastSelectedPosition() ? position + " [s]" : position) +
                    "], count = [" + count + "]");
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d(TAG, "onRemoved() called with: position = [" + position + "], count = [" + count + "]");
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d(TAG, "onMoved() called with: fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "]");
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
//              String newValue = ((Bundle)payload).getString("Name", "xxx");
//                Log.v(TAG, "onChanged: > new value: " + newValue);
                Log.d(TAG, "onChanged() called with: position = [" +
                    (position == getLastSelectedPosition() ? position + " [s]" : position) +
                    "], count = [" + count + "], payload = [" + payload + "] " + data.get(position).getName());
//                ((StickyLinearLayoutManager)((IssuesUiOps)eventListener).getRv().getLayoutManager())
//                    .refreshViewText(">>>");
            }
        });*/
        data = newData;
//        if (getLastSelectedPosition() > -1) {
//          name = getSelected().getName();
//          Log.i(TAG, "updateData: updated name.");
//        }
//        Log.v(TAG, "updateData: e< " + name);
    }


    @Override
    public void accept(List list) throws Exception {
//        Log.w(TAG, "accept() called with: list = [" + list + "]");
//        for (int i = 0; i < list.size(); i++) {
//            IssueData issueData = (IssueData) list.get(i);
//            Log.w(TAG, "new data: " + issueData.getName());
//        }
//        for (int i = 0; i < data.size(); i++) {
//            IssueData issueData = (IssueData) data.get(i);
//            Log.v(TAG, "old data: " + issueData.getName());
//        }
        updateData(list);
    }

    @Override
    public String getToolbarData(Context context, int position) {
        return UiUtil.populateInfoString(context.getResources().getString(R.string._issues_info_template), data.get(position));
    }

    @Override
    public boolean canRemoveItem(int position) {
        if (data != null && position > -1 && position < data.size()) {
            return PermissionsUtil.canRemoveIssue(data.get(position));
        }
        return false;
    }
}
