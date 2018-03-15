package org.rares.miner49er.issues.adapter;

import android.view.View;
import android.widget.TextView;

import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.issues.model.IssueData;

import butterknife.BindView;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesViewHolder extends ResizeableViewHolder

{
    private static final String TAG = IssuesViewHolder.class.getSimpleName();

    @BindView(R.id.tv_resource_name_item)
    TextView issueName;

//    @BindView(R.id.resizeable_list_item_container)
//    LinearLayout issueContainer;

    private IssuesViewProperties ivp = new IssuesViewProperties();

    public IssuesViewHolder(View itemView) {
        super(itemView);
        setItemProperties(ivp);
    }

    @Override
    public void bindData(Object o) {
        IssueData data = (IssueData) o;
        issueName.setText(data.getName());
        ivp.setSelected(false);
        ivp.setText(data.getName());
        ivp.setItemContainerCustomId(data.getId());
    }
}
