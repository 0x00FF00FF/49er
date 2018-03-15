package org.rares.miner49er.entries.adapter;

import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.entries.model.TimeEntryData;

import butterknife.BindView;

/**
 * @author rares
 * @since 14.03.2018
 */

@SuppressWarnings("all")
public class TimeEntriesViewHolder extends ResizeableViewHolder {

    private TimeEntryViewProperties tvp = new TimeEntryViewProperties();

    @BindView(R.id.tv_resource_name_item)
    TextView teTextView;

    @Override
    public void bindData(Object data) {
        TimeEntryData entryData = (TimeEntryData) data;
        String entryDate = new DateTime(entryData.getDate()).toString();
        teTextView.setText("Time entry for: " + entryDate + " | " + entryData.getHours());
        tvp.setText(teTextView.getText().toString());
        tvp.setItemContainerCustomId(entryData.getId());
    }

    public TimeEntriesViewHolder(View itemView) {
        super(itemView);
        setItemProperties(tvp);
    }

}
