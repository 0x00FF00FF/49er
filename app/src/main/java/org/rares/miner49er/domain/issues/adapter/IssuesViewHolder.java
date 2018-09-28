package org.rares.miner49er.domain.issues.adapter;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.util.TextUtils;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesViewHolder extends ResizeableItemViewHolder

{
    private static final String TAG = IssuesViewHolder.class.getSimpleName();

    @BindView(R.id.tv_resource_name_item)
    TextView issueName;

//    @BindView(R.id.resizeable_list_item_container)
//    LinearLayout issueContainer;


    public IssuesViewHolder(View itemView) {
        super(itemView);
        setItemProperties(new IssuesViewProperties());
    }

    @Override
    public void bindData(Object o, boolean shortVersion, boolean selected) {
        IssueData data = (IssueData) o;
        shortTitle = TextUtils.extractInitials(data.getName());
        longTitle = data.getName();

        itemView.setBackgroundColor(data.getColor());
        getItemProperties().setItemBgColor(data.getColor());
        getItemProperties().setId(data.getId());

        validateItem(shortVersion, selected);
    }

    @Override
    public void toggleItemText(boolean shortVersion) {
        issueName.setText(shortVersion ? shortTitle : longTitle);
    }

    private void validateItem(boolean shortVersion, boolean selected) {
        issueName.setRotation(shortVersion ? selected ? 0 : -90 : 0);
        issueName.setGravity(shortVersion ? Gravity.CENTER : Gravity.CENTER | Gravity.START);
        issueName.setText(shortVersion ? shortTitle : longTitle);
    }
}
