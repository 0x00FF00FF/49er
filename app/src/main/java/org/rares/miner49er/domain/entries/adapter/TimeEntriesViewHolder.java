package org.rares.miner49er.domain.entries.adapter;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import org.joda.time.DateTime;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.util.TextUtils;

/**
 * @author rares
 * @since 14.03.2018
 */

@SuppressWarnings("all")
public class TimeEntriesViewHolder extends ResizeableItemViewHolder {

    @BindView(R.id.user_initials_tv)
    TextView userInitials;

    @BindView(R.id.hours_tv)
    TextView hours;

    @BindView(R.id.day_tv)
    TextView day;

    public TimeEntriesViewHolder(View itemView) {
        super(itemView);
        setItemProperties(new TimeEntryViewProperties());
//        userInitials.setTypeface(typefaceLight);
//        day.setTypeface(typefaceLight);
    }

    @Override
    public void bindData(Object data, boolean shortVersion, boolean selected) {
        TimeEntryData entryData = (TimeEntryData) data;
//        itemView.setBackgroundColor(entryData.getColor());
//        Log.i(TAG, "bindData: " + entryData);
        Drawable d = itemView.getBackground();
        if (d instanceof LayerDrawable) {
            d.mutate();
            LayerDrawable ld = (LayerDrawable) d;
            GradientDrawable opaqueBackground = (GradientDrawable) ld.findDrawableByLayerId(R.id.opaque_background);
            if (opaqueBackground != null) {
                opaqueBackground.setColor(entryData.getColor());
            }
        }

        userInitials.setText(TextUtils.extractInitials(entryData.getUserName()));
        hours.setText(String.valueOf(entryData.getHours()) + (entryData.getHours() > 1 ? " hours" : " hour"));

        DateTime dateTime = new DateTime(entryData.getWorkDate());
        String pattern = "dd MMM" + (dateTime.year().get() < DateTime.now().year().get() ? " yyyy" : "");
        String entryDate = dateTime.toString(pattern);

        day.setText(entryDate);
        getItemProperties().setId(entryData.id);
    }

}
