package org.rares.miner49er.domain.entries.adapter;

import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.entries.model.TimeEntryData;

/**
 * @author rares
 * @since 14.03.2018
 */

@SuppressWarnings("all")
public class TimeEntriesViewHolder extends ResizeableItemViewHolder {

    @BindView(R.id.tv_resource_name_item)
    TextView teTextView;

    @Override
    public void bindData(Object data, boolean shortVersion, boolean selected) {
        TimeEntryData entryData = (TimeEntryData) data;
        itemView.setBackgroundColor(entryData.getColor());
        teTextView.setText(entryData.toString());
    }

    public TimeEntriesViewHolder(View itemView) {
        super(itemView);
        setItemProperties(new TimeEntryViewProperties());
    }

}
